FROM gradle:8.14-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew
# Jätame generateJooq siin vahele, eraldi container selle jaoks
RUN ./gradlew clean build -x generateJooq --stacktrace

FROM eclipse-temurin:21-jdk-ubi9-minimal
WORKDIR /app
COPY --from=build /workspace/eewn-rest/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
