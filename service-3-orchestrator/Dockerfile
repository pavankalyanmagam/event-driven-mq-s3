# Stage 1: Build the application using a Maven image
FROM maven:3.9.6-eclipse-temurin-17-focal AS build

# Set the working directory
WORKDIR /app

# Copy the entire project context (which is now the root project folder)
COPY . .

# Run the Maven build from the root directory. This will build all modules.
RUN mvn clean install -DskipTests

# Stage 2: Create the final, slim image
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# This is the key change: We now specify the path to the correct jar file
# for each service from the 'target' folder of its module.
# You will create a SEPARATE Dockerfile for each service with the correct path.
# THIS EXAMPLE IS FOR service-1-api.
COPY --from=build /app/service-1-api/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]