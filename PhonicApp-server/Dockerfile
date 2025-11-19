FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ./target/PhonicApp-server.jar app.jar

RUN apt-get update && apt-get install -y ffmpeg


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
