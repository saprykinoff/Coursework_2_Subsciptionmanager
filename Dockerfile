FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
#ARG TOKEN
#ENV ONLINECASHIER_TOKEN $TOKEN
COPY build/libs/subscribeManager-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

#LOCAL:  ./gradlew build && docker build . -t sprff/subscribemanager && docker push sprff/subscribemanager
#REMOTE: docker pull sprff/subscribemanager && docker run -e SUBSCRIBEMANAGER_TOKEN=$SUBSCRIBEMANAGER_TOKEN sprff/subscribemanager