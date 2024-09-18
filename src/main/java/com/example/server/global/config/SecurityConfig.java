package com.example.server.global.config;

import com.example.server.domain.member.dto.MemberResponseDto;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.auth.oauth2.domain.AccessTokenAuthenticationProvider;
import com.example.server.global.auth.oauth2.filter.OAuth2AccessTokenAuthenticationFilter;
import com.example.server.global.auth.security.filter.SilentReAuthenticationFilter;
import com.example.server.global.auth.security.service.CustomUserDetailsService;
import com.example.server.global.auth.security.service.JwtService;
import com.example.server.global.auth.security.filter.JsonUsernamePasswordAuthenticationFilter;
import com.example.server.global.auth.security.filter.JwtAuthenticationFilter;
import com.example.server.global.auth.security.handler.*;
import com.example.server.global.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Lazy
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final CorsProperties corsProperties;
    private final AccessTokenAuthenticationProvider provider;
    private final RedisUtil redisUtil;

    // 스프링 시큐리티 기능 비활성화
    @Bean
    public WebSecurityCustomizer configure() {
        return (web -> web.ignoring()
//				.requestMatchers(toH2Console())
                .requestMatchers( "/error", "/swagger-ui/**",
                        "/swagger-resources/**", "/v3/api-docs/**")
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http	.csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
                        .configurationSource(corsConfigurationSource()))
                .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 DB
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers( "/api/member/signup", "/api/member/isDuplicated", "/api/member/login/oauth").permitAll()
                        .requestMatchers( "/api/email/send", "/api/member/password", "/api/email/confirm", "/api/member/find", "/api/member/interest/my").permitAll()
                        .requestMatchers("/api/post/all", "/api/ootd/all", "/api/post/count/all", "/api/post/info/{postId}", "/api/ootd/info/{postId}").permitAll()
                        .requestMatchers("/api/search/**").permitAll()
                        .requestMatchers("/api/member/profile", "/api/post/by-member", "/api/ootd/by-member", "/api/post/count/by-member").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout
                        .logoutUrl("/api/member/logout")
                        .addLogoutHandler(logoutHandler())
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .deleteCookies("refreshToken"))
        ;
        http
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new CustomAccessDeniedHandler(jwtService))
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .addFilterAfter(silentReAuthenticationFilter(), LogoutFilter.class)
                .addFilterAfter(jsonUsernamePasswordLoginFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(auth2AccessTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {//2 - AuthenticationManager 등록
        DaoAuthenticationProvider provider = daoAuthenticationProvider();//DaoAuthenticationProvider 사용
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler(){
        return new LoginSuccessJWTProvideHandler(jwtService, memberRepository, redisUtil);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(){
        return new LoginFailureHandler();
    }



    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() throws Exception {
        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonUsernamePasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter(){
        return new JwtAuthenticationFilter(jwtService, memberRepository, redisUtil);
    }

    @Bean
    OAuth2AccessTokenAuthenticationFilter auth2AccessTokenAuthenticationFilter() {
        return new OAuth2AccessTokenAuthenticationFilter(provider, loginSuccessJWTProvideHandler(), loginFailureHandler());
    }

    @Bean
    public SilentReAuthenticationFilter silentReAuthenticationFilter() {
        return new SilentReAuthenticationFilter(jwtService, memberRepository);
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        corsConfiguration.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        corsConfiguration.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins(), "http://localhost:3000", "https://trippy-api.store"));
        corsConfiguration.setExposedHeaders(Arrays.asList("Authorization", "Authorization-refresh"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(corsConfiguration.getMaxAge());

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfigSource;
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (response.getStatus() != 401) {
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write(objectMapper.writeValueAsString(
                        ApiResponse.onSuccess(MemberResponseDto.MemberTaskSuccessResponseDto.builder().isSuccess(true).build())));
            }
        };
    }

    @Bean
    public LogoutHandler logoutHandler() {
        return new LogoutHandlerImpl(memberRepository, jwtService, redisUtil, objectMapper);
    }


//    @Bean
//    public CorsConfigurationSource corsConfiguration() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
//        configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Content-Type", "Authorization", "X-XSRF-token", "Authorization-refresh"));
//        configuration.setAllowCredentials(true);
//        configuration.addAllowedOriginPattern("http://localhost:3000");
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "..."));
//        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        configuration.setAllowCredentials(true);
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Authorization-refresh", "Cache-Control", "Content-Type"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

}
