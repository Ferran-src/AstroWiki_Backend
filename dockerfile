# Usa una imagen base de Amazon Corretto 25 JRE en Alpine Linux
# Es la versión más reciente disponible en Corretto según la lista.
FROM amazoncorretto:25-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de Gradle (build.gradle.kts, settings.gradle.kts) y el directorio gradle/wrapper
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./

# Instala bash (opcional, pero ./gradlew puede necesitarlo en Alpine)
RUN apk add --no-cache bash

# Descarga las dependencias de Gradle
RUN ./gradlew build --exclude-task copyDockerfile --exclude-task copyDockerIgnore -x compileKotlin -x compileTestKotlin --no-daemon

# Copia el resto del código fuente
COPY . .

# Hace que el script gradlew sea ejecutable
RUN chmod +x ./gradlew

# Construye el JAR final
RUN ./gradlew shadowJar --exclude-task copyDockerfile --exclude-task copyDockerIgnore --no-daemon

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "build/libs/AstroWiki-BackEnd-all.jar"]