# syntax=docker/dockerfile:1

# --- Build stage ---
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /build

# Copy the whole repo so the parent pom and module are available
COPY . .

# Build only the buzz module; compile upstream modules as needed; skip tests for speed
RUN ./mvnw -q -DskipTests -pl buzz -am clean package

# --- Runtime stage ---
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copy the fat jar produced in the build stage
COPY --from=build /build/buzz/target/buzz-*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]