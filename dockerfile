# Usa una imagen base de Amazon Corretto 25 JRE en Alpine Linux
FROM amazoncorretto:25-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia TODOS los archivos del proyecto (incluido gradlew, gradle/, build.gradle.kts, settings.gradle.kts, src/, etc.)
COPY . .

# Hace que el script gradlew sea ejecutable
RUN chmod +x ./gradlew

# Instala bash (opcional, pero ./gradlew puede necesitarlo en Alpine)
RUN apk add --no-cache bash

# Descarga las dependencias de Gradle y realiza una compilación parcial para poblar la caché
# Excluimos tareas de copia y compilación de Kotlin/Tests para acelerar esta etapa de caché
RUN ./gradlew build --exclude-task copyDockerfile --exclude-task copyDockerIgnore -x compileKotlin -x compileTestKotlin --no-daemon

# Construye el JAR final (excluyendo tareas de copia si las tienes)
RUN ./gradlew shadowJar --exclude-task copyDockerfile --exclude-task copyDockerIgnore --no-daemon

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "build/libs/AstroWiki-BackEnd-all.jar"]