FROM maven:3.8.6-jdk-11 AS MAVEN_BUILD
COPY ./ ./
#RUN $(ls -l)
#RUN mkdir -p /root/.m2 \
#    && mkdir -p /root/.m2/repository/org/springframework/boot/spring-boot-starter-parent/2.6.5
# COPY .m2/settings-docker.xml /usr/share/maven/ref/settings-docker.xml
#RUN mvn -B -f /pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
RUN mvn -Dmaven.test.skip clean package
#FROM openjdk:11.0.7-jdk-slim
#COPY --from=MAVEN_BUILD /target/telegram-bot-0.0.1-SNAPSHOT.jar /backend.jar
#CMD ["java", "-jar", "/target/telegram-bot-0.0.1-SNAPSHOT.jar"]
FROM openjdk:11.0.7-jdk-slim AS APP_COPY
COPY --from=MAVEN_BUILD /target/telegram-bot-0.0.1-SNAPSHOT.jar /backend.jar
CMD ["java", "-jar", "/backend.jar"]
