package com.example.server.global.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@ConfigurationProperties(prefix = "app")
public class AppProperties {

//    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Auth {
//        private String tokenSecret;
//        private long tokenExpiry;
//        private long refreshTokenExpiry;
//    }

    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();

        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        public OAuth2 authorizedRedirectUris(List<String> authorizedRedirectUris) {
            this.authorizedRedirectUris = authorizedRedirectUris;
            return this;
        }
    }
}

