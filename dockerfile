# Usa una imagen base de OpenJDK 21 LTS (Oracle Linux Slim)
FROM openjdk:21-jdk-oraclelinux8

# Resto del Dockerfile es idéntico
WORKDIR /app

COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./

RUN ./gradlew build --exclude-task copyDockerfile --exclude-task copyDockerIgnore -x compileKotlin -x compileTestKotlin --no-daemon

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew shadowJar --exclude-task copyDockerfile --exclude-task copyDockerIgnore --no-daemon

ENTRYPOINT ["java", "-jar", "build/libs/AstroWiki-BackEnd-all.jar"]