FROM openjdk:17
WORKDIR /app
# 빌드된 Spring Boot JAR 파일을 복사
COPY build/libs/server-0.0.1-SNAPSHOT.jar trippyj.jar

# JAR 파일 실행
CMD ["java", "-jar", "trippyj.jar", "--spring.profiles.active=prod-profile"]