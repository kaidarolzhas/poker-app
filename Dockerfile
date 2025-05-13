
FROM maven:3.9.4-eclipse-temurin-21 AS build


RUN apt-get update && \
    apt-get install -y curl git && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g bower

WORKDIR /app


COPY . .


WORKDIR /app/src/main/resources/static
RUN bower install --allow-root || echo "Bower failed or nothing to install"


WORKDIR /app
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jdk

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

WORKDIR /app


COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
