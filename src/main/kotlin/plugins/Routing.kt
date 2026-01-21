package org.example.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import org.example.routes.*

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
           articuloRoutes()
            usuarioRoutes()
            categoriaRoutes()
            articuloCategoriaRoutes()
            comentarioRoutes()
            postRoutes()
            postLikesRoutes()
            comentarioLikesRoutes()
            dynamicQueryRoutes()
            seccionRoutes()
            seguimientoSeccionesRoutes()
        }
        route("/imagen"){
            imagenRoutes()
        }
        // Ruta raíz u otras rutas que no necesiten un prefijo
        get("/") {
            call.respondText("Bienvenido a la API de Astrowiki!")
        }
    }
}