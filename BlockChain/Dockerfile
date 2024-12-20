# Use an official OpenJDK runtime as a parent image
# Use a base image with Java installed
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled Java program (assumes you compile it locally first)
COPY ./target/Blockchain-mainNode-1.0-SNAPSHOT.jar .

# Expose the port that the node listens on (e.g., 6000)
EXPOSE 6000

# Command to start the application
CMD ["java", "-jar", "Blockchain-mainNode-1.0-SNAPSHOT.jar"]