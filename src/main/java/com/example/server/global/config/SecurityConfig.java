//package com.example.server.global.config;
//
//import com.example.server.domain.member.repository.MemberRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//@Lazy
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final OAuthAuthenticationSuccessHandler oauthAuthenticationSuccessHandler;
//    private final OAuthAuthenticationFailureHandler oauthAuthenticationFailureHandler;
//    private final HttpCookieOAuthAuthorizationRequestRepository httpCookieOAuthAuthorizationRequestRepository;
//    private final ExceptionHandlerFilter exceptionHandlerFilter;
//
//    // 스프링 시큐리티 기능 비활성화
//    @Bean
//    public WebSecurityCustomizer configure() {
//        return (web -> web.ignoring()
////				.requestMatchers(toH2Console())
//                .requestMatchers("/fcm", "/static/**", "/h2-console/**",
//                        "/favicon.ico", "/error", "/swagger-ui/**",
//                        "/swagger-resources/**", "/v3/api-docs/**")
//        );
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http	.csrf(AbstractHttpConfigurer::disable)
//                .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 DB
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers( "/api/member/signup", "/", "/api/member/login", "/api/member/isDuplicated", "/api/email/send").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .anyRequest().authenticated())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .oauth2Login(oauth2Login -> oauth2Login
//                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
//                                .baseUri("/oauth2/authorize")
//                                .authorizationRequestRepository(httpCookieOAuthAuthorizationRequestRepository))
//                        .redirectionEndpoint(redirectionEndpoint -> redirectionEndpoint
//                                .baseUri("/oauth2/callback/*"))
//                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
//                                .userService(oAuth2UserService))
//                        .successHandler(oauthAuthenticationSuccessHandler)
//                        .failureHandler(oauthAuthenticationFailureHandler))
//        ;
////		http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
//        http
//                .addFilterBefore(jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//
//        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//
//        return daoAuthenticationProvider;
//    }
//
//    @Bean
//    public static PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager() throws Exception {//2 - AuthenticationManager 등록
//        DaoAuthenticationProvider provider = daoAuthenticationProvider();//DaoAuthenticationProvider 사용
//        return new ProviderManager(provider);
//    }
//
//    @Bean
//    public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler(){
//        return new LoginSuccessJWTProvideHandler(jwtService, memberRepository);
//    }
//
//    @Bean
//    public LoginFailureHandler loginFailureHandler(){
//        return new LoginFailureHandler();
//    }
//
//    @Bean
//    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() throws Exception {
//        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
//        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
//        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
//        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
//        return jsonUsernamePasswordLoginFilter;
//    }
//
//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationProcessingFilter(){
//        return new JwtAuthenticationFilter(jwtService, memberRepository);
//    }
//}
