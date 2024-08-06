package com.example.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.google.firebase.FirebaseApp.DEFAULT_APP_NAME;

@Configuration
@Slf4j
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

            // 디버깅 로그 추가
            String jsonContent = new String(credentialsStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("Firebase credentials content: {}", jsonContent);

            // 스트림을 다시 사용하기 위해 초기화
            credentialsStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));

            return GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(scope);
        } catch (IOException e) {
            log.error("Failed to load Firebase credentials from path: " + path, e);
            throw new RuntimeException("Failed to load Firebase credentials from path: " + path, e);
        }
    }

}
