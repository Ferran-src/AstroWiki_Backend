package org.example

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.engine.*
import io.ktor.server.netty.*


fun main() {

    embeddedServer(Netty, port = 8080) { // o System.getenv("PORT")
    }.start(wait = true)


}