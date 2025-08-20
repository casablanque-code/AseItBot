# Этап сборки (Gradle)
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Этап рантайма
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Порт — если бот слушает HTTP (иначе можно убрать)
#EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
