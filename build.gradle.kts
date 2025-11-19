plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Ktor Server Core (núcleo del servidor)
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")

    // Motor de servidor Netty (hay otros como Jetty o CIO)
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")

    // Serialización (para manejar JSON, XML, etc.)
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")

    // Manejo de rutas y parámetros
    implementation("io.ktor:ktor-server-routing-jvm:2.3.12")

    // CORS (Permite solicitudes desde otros dominios, crucial para frontend web)
    implementation("io.ktor:ktor-server-cors-jvm:2.3.12")

    // Logging (útil para depurar y monitorear)
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.12")

    // Logging backend (ej: Logback)
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- Para la parte de Base de Datos ---
    // Exposed (Framework para acceso a BD)
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")

    // Driver de la base de datos (ej: PostgreSQL, H2, SQLite)
    // Ejemplo para H2 (base de datos en memoria, útil para pruebas/desarrollo)
    implementation("com.h2database:h2:2.2.224")
    // Ejemplo para PostgreSQL (más común en producción)
    // implementation("org.postgresql:postgresql:<version_postgresql>")
    // Ejemplo para SQLite
    // implementation("org.xerial:sqlite-jdbc:<version_sqlite>")

    // Pool de conexiones (recomendado para producción)
    implementation("com.zaxxer:HikariCP:6.3.0") // Para Java 11+

    // --- Para Testing (opcional pero recomendado) ---
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:<version_kotlin>")

    // env para las credentiales
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // --- Cliente Ktor (si tu backend necesita hacer solicitudes a otras APIs) ---
    // implementation("io.ktor:ktor-client-core-jvm:2.3.12")
    // implementation("io.ktor:ktor-client-cio-jvm:2.3.12") // Motor cliente
    // implementation("io.ktor:ktor-client-content-negotiation:2.3.12")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}