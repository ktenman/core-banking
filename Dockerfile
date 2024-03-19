FROM gradle:jdk21 AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle
COPY src /app/src

RUN gradle clean build -x test --no-daemon

FROM bellsoft/liberica-runtime-container:jre-21-slim-musl

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS="-Xms512m -Duser.timezone=Europe/Tallinn"
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80"

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
