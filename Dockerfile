# ===== build stage =====
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /app
COPY . .
# если у тебя есть тесты, и они долгие — можно добавить -x test
RUN gradle --no-daemon clean bootJar

# ===== runtime stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
EXPOSE 8080 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
