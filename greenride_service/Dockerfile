# distributed/greenride_service/Dockerfile

# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create a directory for the H2 database
RUN mkdir -p data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]