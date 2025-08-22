
# Use an official OpenJDK runtime as a parent image
FROM openjdk:19-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Argument to specify the JAR file path
ARG JAR_FILE=target/*.jar

# Copy the executable JAR file from the target folder to the container
COPY ${JAR_FILE} application.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the JAR file 
ENTRYPOINT ["java","-jar","/app/application.jar"]
