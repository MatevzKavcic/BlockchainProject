# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy your project JAR file or compiled classes into the container
COPY ./target/Blockchain-SideNode-1.0-SNAPSHOT.jar .

# Expose a port (adjust to your application's port)
EXPOSE 8080

# Define the command to run your application
CMD ["java", "-jar", "Blockchain-SideNode-1.0-SNAPSHOT.jar"]