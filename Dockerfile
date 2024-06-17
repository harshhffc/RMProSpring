FROM openjdk:17-jdk

WORKDIR /app
COPY build/libs/rmproserver-0.0.1-SNAPSHOT.war /app/rms.war
EXPOSE 8444
CMD ["java", "-jar", "rms.war"]
