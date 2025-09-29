# Stage 1: Build the JAR
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/jobportal-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
