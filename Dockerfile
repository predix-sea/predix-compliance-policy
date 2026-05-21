FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S predix && adduser -S predix -G predix
COPY target/predix-compliance-policy-*.jar app.jar
USER predix
EXPOSE 8095
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
