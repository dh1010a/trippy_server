package com.example.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static com.google.firebase.FirebaseApp.DEFAULT_APP_NAME;

@Configuration
public class FcmConfig {

    @Value("${fcm.private.key.path}")
    private String path;

    @Value("${fcm.private.key.scope}")
    private String scope;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance(getFirebaseApp());
    }

    @SneakyThrows
    private FirebaseApp getFirebaseApp() {
        return FirebaseApp.getApps().stream()
                .filter(app -> app.getName().equals(DEFAULT_APP_NAME))
                .findFirst()
                .orElseGet(this::createFirebaseApp);
    }

    @SneakyThrows
    private FirebaseApp createFirebaseApp() {
        GoogleCredentials credentials = createGoogleCredentials();
        credentials.refreshIfExpired();
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    private GoogleCredentials createGoogleCredentials() {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            InputStream credentialsStream = resource.getInputStream();
            return GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(scope);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
