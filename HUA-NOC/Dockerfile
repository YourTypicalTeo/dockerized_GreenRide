# distributed/HUA-NOC/Dockerfile

# 1. Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy configuration and source code
COPY pom.xml .
COPY src ./src

# Build the application (skipping tests to speed up deployment)
RUN mvn clean package -DskipTests

# 2. Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port defined in application.yaml
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]