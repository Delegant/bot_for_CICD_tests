FROM maven:3.6.3-jdk-11
COPY ./ ./
RUN mvn clean package
ENTRYPOINT ["java", "-jar", "backend.jar"]
