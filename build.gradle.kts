plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.0.0"
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

    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")


    implementation("io.ktor:ktor-server-cors-jvm:2.3.12")

    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.12")
    //logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Exposed (Framework para acceso a BD)
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")



    implementation("com.h2database:h2:2.2.224")
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("com.zaxxer:HikariCP:6.3.0")

    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.12")

    // env para las credentiales
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("at.favre.lib:bcrypt:0.10.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}