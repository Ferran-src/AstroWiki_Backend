# Usa una imagen base de OpenJDK 17 o 21 LTS
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de Gradle (build.gradle.kts, settings.gradle.kts) y el directorio gradle/wrapper
# Es importante copiar estos primero para aprovechar la caché de capas de Docker si no cambian
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./


RUN ./gradlew build --exclude-task copyDockerfile --exclude-task copyDockerIgnore -x compileKotlin -x compileTestKotlin --no-daemon

COPY . .

# COPY render.yaml ./

# Construye el JAR final (excluyendo tareas de copia si las tienes)
RUN ./gradlew shadowJar --exclude-task copyDockerfile --exclude-task copyDockerIgnore --no-daemon

# Define el comando para ejecutar la aplicación
# Asegúrate de que el nombre del JAR coincida con el generado por ShadowJar
# Por defecto, ShadowJar suele generar something-all.jar o something-shadow.jar
ENTRYPOINT ["java", "-jar", "build/libs/AstroWiki-BackEnd-all.jar"]
