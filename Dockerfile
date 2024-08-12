FROM tomcat:9.0

WORKDIR /app
COPY build/libs/rmproserver-0.0.1-SNAPSHOT.war /app/rms.war
EXPOSE 8080

RUN mkdir -p /var/www/images/document_picture/

CMD ["catalina.sh", "run"]
