# ---------- STAGE 1: BUILD ----------
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

RUN apk add --no-cache bash

# Copiamos todo el proyecto
COPY . .

RUN chmod +x ./gradlew

# Construimos el fat jar
RUN ./gradlew shadowJar --no-daemon

# ---------- STAGE 2: RUNTIME ----------
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copiamos solo el jar final desde el builder
COPY --from=builder /app/build/libs/AstroWiki-BackEnd-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]