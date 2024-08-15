package com.example.server.global.auth.oauth2.model;

import org.springframework.http.HttpMethod;

public enum SocialType {


    KAKAO(
            "kakao",
            "https://kapi.kakao.com/v2/user/me",
            "https://kapi.kakao.com/v1/user/unlink",
            HttpMethod.GET,
            HttpMethod.POST
    ),

    GOOGLE(
            "google",
            "https://www.googleapis.com/oauth2/v3/userinfo",
            "https://accounts.google.com/o/oauth2/revoke",
            HttpMethod.GET,
            HttpMethod.POST
    ),

    NAVER(
            "naver",
            "https://openapi.naver.com/v1/nid/me",
            "https://nid.naver.com/oauth2.0/authorize",
            HttpMethod.GET,
            HttpMethod.POST
    ),
    LOCAL(
            "local",
            "",
            "",
            HttpMethod.GET,
            HttpMethod.POST
    )
    ;



    private final String socialName;
    private final String userInfoUrl;
    private final String unlinkUrl;
    private final HttpMethod method;
    private final HttpMethod unlinkMethod;

    SocialType(String socialName, String userInfoUrl, String unlinkUrl, HttpMethod method, HttpMethod unlinkMethod) {
        this.socialName = socialName;
        this.userInfoUrl = userInfoUrl;
        this.unlinkUrl = unlinkUrl;
        this.method = method;
        this.unlinkMethod = unlinkMethod;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpMethod getUnlinkMethod() {
        return unlinkMethod;
    }

    public String getSocialName() {
        return socialName;
    }

    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    public String getUnlinkUrl() {
        return unlinkUrl;
    }
}