FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Use a consistent Gradle home directory to enable caching
RUN ./gradlew bootJar --gradle-user-home /gradle/home

FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=/app/build/libs/weatherapi.jar
COPY --from=builder ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]