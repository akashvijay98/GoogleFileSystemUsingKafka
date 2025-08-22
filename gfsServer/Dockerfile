# Use a specific and lightweight OpenJDK runtime as a parent image
FROM openjdk:19-jdk-slim

# Set the working directory in the container
WORKDIR /app

RUN mkdir -p /temp2
RUN chmod -R 777 /temp2

# Argument to specify the JAR file path, making it more flexible
ARG JAR_FILE=target/*.jar

# Copy the executable JAR file to the container
COPY ${JAR_FILE} application.jar

# Run the JAR file
ENTRYPOINT ["java","-jar","/app/application.jar"]