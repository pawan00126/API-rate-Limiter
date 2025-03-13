# Use Java 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the JAR file (built locally or via Maven)
COPY target/*.jar API-Rate-Limiter-0.0.1-SNAPSHOT.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
