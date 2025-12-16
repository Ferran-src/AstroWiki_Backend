package org.example

import io.ktor.server.application.Application
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.example.database.DatabaseFactory

import org.example.plugins.configureRouting

fun main() {
    //Iniciacion del server asincrono
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)


}
fun Application.module() {
    DatabaseFactory.init()
    configureRouting()       // Registra las rutas (endpoints)
}