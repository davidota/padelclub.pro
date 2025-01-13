FROM eclipse-temurin:17-jre
COPY target/*.jar app.jar
COPY src/main/resources/setting.json /config/setting.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
