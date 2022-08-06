FROM maven:3.6.3-jdk-11
COPY /home/jenkins/workspace/job_build_and_run_into_doker ./
RUN mvn clean package
ENTRYPOINT ["java", "-jar", "backend.jar"]
