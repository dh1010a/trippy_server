package com.example.server;

import com.example.server.global.config.AppProperties;
import com.example.server.global.config.CorsProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({
        CorsProperties.class,
        AppProperties.class
})
public class ServerApplication {

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("현재 시각 = " + new Date());
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
