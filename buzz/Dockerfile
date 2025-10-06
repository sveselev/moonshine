# Runtime image using Amazon Corretto 21 (Java 21)
FROM amazoncorretto:21-alpine

# Set a non-root working directory
WORKDIR /app

# Copy the Spring Boot fat jar built locally (make sure you run `mvn -pl buzz -am package` first)
COPY target/buzz-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Optional JVM tuning hooks
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]