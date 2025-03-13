# Use Java 21 base image
FROM eclipse-temurin:21-jdk-jammy AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper & pom.xml first (to cache dependencies)
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Give execution permission to Maven wrapper
RUN chmod +x mvnw

# Download dependencies first (improves caching)
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Use a smaller JDK for running the app
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
