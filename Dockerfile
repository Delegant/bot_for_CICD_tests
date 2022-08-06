FROM maven:3.6.3-jdk-11
COPY ./ ./
RUN maven -DskipTests clean package
ENTRYPOINT ["java", "-jar", "backend.jar"]
