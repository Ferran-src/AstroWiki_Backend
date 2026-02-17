# Usa una imagen base de Amazon Corretto 25 JRE en Alpine Linux
FROM amazoncorretto:25-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos del wrapper de Gradle, el directorio gradle/, y los archivos de configuración principales
# Estos archivos cambian con menos frecuencia, permitiendo usar la caché de capas de Docker
COPY gradle ./gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./

# Hace que el script gradlew sea ejecutable (importante antes de usarlo)
RUN chmod +x ./gradlew

# Instala bash (opcional, pero ./gradlew puede necesitarlo en Alpine)
RUN apk add --no-cache bash

# Descarga las dependencias de Gradle (y realiza tareas previas a la compilación si es necesario)
# Excluimos tareas de compilación de Kotlin/Tests y tareas de copia para acelerar esta etapa de caché
# La tarea 'dependencies' solo resuelve dependencias, 'build' puede hacer más cosas pero también resuelve dependencias.
# Si 'build' falla aquí por falta de código fuente, puedes intentar 'resolveDependencies' o 'dependencies'
# pero 'build' con exclusiones a veces funciona si no hay dependencias de código compilado en la configuración.
# Si falla, intenta solo: RUN ./gradlew dependencies --no-daemon
RUN ./gradlew build --exclude-task copyDockerfile --exclude-task copyDockerIgnore -x compileKotlin -x compileTestKotlin --no-daemon || ./gradlew dependencies --no-daemon

# Copia el resto del código fuente (src/, otros archivos)
COPY . .

# Asegúrate de que gradlew tenga permisos de ejecución de nuevo (por si acaso COPY lo sobrescribió, aunque no debería)
# No es estrictamente necesario si COPY no sobreescribe gradlew, pero es una precaución.
# RUN chmod +x ./gradlew # Descomenta si surge error de permisos de nuevo

# Construye el JAR final (excluyendo tareas de copia si las tienes)
RUN ./gradlew shadowJar --exclude-task copyDockerfile --exclude-task copyDockerIgnore --no-daemon

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "build/libs/AstroWiki-BackEnd-all.jar"]