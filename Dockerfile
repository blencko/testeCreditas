FROM gradle:8.11.1-jdk21 as build

WORKDIR /app

COPY src/app

RUN gradle build --no-daemon -x test

FROM  ghcr.io/graalvm/jdk-community:21

ENV APP_DIRECTORY=/opt/app

WORKDIR $APP_DIRECTORY
COPY --from=build /app/build/libs/testecreditas-0.0.1-SNAPSHOT.jar $APP_DIRECTORY

EXPOSE 80
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xmx1g", "/opt/app/testecreditas-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]