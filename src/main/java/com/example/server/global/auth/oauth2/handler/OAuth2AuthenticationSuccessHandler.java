package com.example.server.global.auth.oauth2.handler;

import com.example.server.domain.member.domain.Member;
import com.example.server.domain.member.model.Role;
import com.example.server.domain.member.repository.MemberRepository;
import com.example.server.global.apiPayload.ApiResponse;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.example.server.global.auth.security.dto.LoginResponseDto;
import com.example.server.global.config.AppProperties;
import com.example.server.global.auth.security.service.JwtService;
import com.example.server.global.auth.security.domain.JwtToken;
import com.example.server.global.auth.security.model.ProviderType;
import com.example.server.global.auth.oauth2.repository.HttpCookieOAuthAuthorizationRequestRepository;
import com.example.server.global.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.example.server.global.auth.oauth2.repository.HttpCookieOAuthAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static com.example.server.global.auth.oauth2.repository.HttpCookieOAuthAuthorizationRequestRepository.REFRESH_TOKEN;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    private final MemberRepository memberRepository;

    private final HttpCookieOAuthAuthorizationRequestRepository authorizationRequestRepository;

    private final AppProperties appProperties;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final int COOKIE_MAX_AGE = 3600; //1시간

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
//        String targetUrl = redirectUri.orElse("http://localhost:3000/login");
        log.info(request.getRequestURI());

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());
        String memberId = extractMemberId(authentication);
        log.info("Success To Authorize. memberID : " + memberId);

        Member member = memberRepository.findByMemberId(extractMemberId(authentication))
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 기존에 가입하지 않은 신규 회원
        if (member.getRole() == Role.ROLE_GUEST) {


        }

        JwtToken jwtToken = jwtService.createJwtToken(authentication);

        member.updateRefreshToken(jwtToken.getRefreshToken());
        member.setProviderType(providerType);


        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, jwtToken.getRefreshToken(), COOKIE_MAX_AGE);

        jwtService.sendAccessAndRefreshToken(response, jwtToken);

        log.info( "로그인에 성공합니다. memberId: {}" , memberId);
        log.info( "AccessToken 을 발급합니다. AccessToken: {}" ,jwtToken.getAccessToken());
        log.info( "RefreshToken 을 발급합니다. RefreshToken: {}" ,jwtToken.getRefreshToken());

        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        String finalTargetUrl =  UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", jwtToken.getAccessToken())
                .build().toUriString();

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");


        LoginResponseDto.LoginDto loginDto = LoginResponseDto.LoginDto.builder()
                .memberId(memberId)
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.onSuccess(loginDto)
        ));
//        getRedirectStrategy().sendRedirect(request, response, finalTargetUrl);

    }

//    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
//                .map(Cookie::getValue);
//
//        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
//            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
//        }
//
//        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
////        String targetUrl = redirectUri.orElse("http://localhost:3000/login");
//        log.info(request.getRequestURI());
//
//        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
//        ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());
//        String memberId = extractMemberId(authentication);
//        log.info("Success To Authorize. memberID : " + memberId);
//
//        Member member = memberRepository.findByMemberId(extractMemberId(authentication))
//                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MEMBER_NOT_FOUND));
//
//        // 기존에 가입하지 않은 신규 회원
//        if (member.getRole() == Role.ROLE_GUEST) {
//
//
//        }
//
//        JwtToken jwtToken = jwtService.createJwtToken(authentication);
//
//        member.updateRefreshToken(jwtToken.getRefreshToken());
//        member.setProviderType(providerType);
//
//
//        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
//        CookieUtil.addCookie(response, REFRESH_TOKEN, jwtToken.getRefreshToken(), COOKIE_MAX_AGE);
//
//        jwtService.sendAccessAndRefreshToken(response, jwtToken);
//
//        log.info( "로그인에 성공합니다. memberId: {}" , memberId);
//        log.info( "AccessToken 을 발급합니다. AccessToken: {}" ,jwtToken.getAccessToken());
//        log.info( "RefreshToken 을 발급합니다. RefreshToken: {}" ,jwtToken.getRefreshToken());
//
//        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
//        context.setAuthentication(authentication);
//        SecurityContextHolder.setContext(context);
//
//        return UriComponentsBuilder.fromUriString(targetUrl)
//                .queryParam("accessToken", jwtToken.getAccessToken())
//                .build().toUriString();
//    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    if(authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                        return true;
                    }
                    return false;
                });
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String extractMemberId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
    private String extractPassword(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getPassword();
    }
}
