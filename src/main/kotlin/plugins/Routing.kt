package org.example.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import org.example.routes.*

fun Application.configureRouting() {
    routing {
        // Agrupar rutas bajo un prefijo común es una buena práctica
        route("/api/v1") {
            // Llama a las funciones que definen rutas específicas
           // articuloRoutes() // Define las rutas para /articulos (GET, POST, etc.)
           // usuarioRoutes()  // Define las rutas para /usuarios (GET, POST, etc.)
           // categoriaRoutes() // Define las rutas para /categorias (GET, POST, etc.)
            // forumRoutes() // Define las rutas para /foro (GET, POST, etc.)
            // imageRoutes() // Define las rutas para /imagenes (GET, POST, etc.)
            dynamicQueryRoutes() // Define la ruta para /query (POST)
        }

        // Ruta raíz u otras rutas que no necesiten un prefijo
        get("/") {
            call.respondText("Bienvenido a la API de Astrowiki!")
        }
    }
}