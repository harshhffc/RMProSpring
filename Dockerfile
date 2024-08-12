FROM openjdk:17-jdk

WORKDIR /app
COPY build/libs/rmproserver-0.0.1-SNAPSHOT.war /app/rms.war
EXPOSE 8080

RUN mkdir -p /var/www/images/document_picture/

CMD ["java", "-jar", "rms.war"]
