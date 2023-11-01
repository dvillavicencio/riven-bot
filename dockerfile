# The following docker file is just a sample and very simple docker build
# Future docker builds will be subject to change depending on the needs of the project
FROM eclipse-temurin:17

COPY ./build/libs/*.jar app.jar
COPY ./keystore.p12 keystore.p12

ENTRYPOINT ["java", "-jar", "/app.jar"]





