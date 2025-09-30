# -------------------------------
# Stage 1: Build the JAR
# -------------------------------
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first (for better caching of dependencies)
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the JAR (skip tests for faster build)
RUN mvn clean package -DskipTests

# -------------------------------
# Stage 2: Run the JAR
# -------------------------------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will map its own $PORT to this)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
