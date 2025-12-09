package org.example.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.Articulo
import org.example.services.ArticuloService

fun Route.articuloRoutes() {

    val service = ArticuloService()

    route("/articulos") {

        // GET /api/v1/articulos
        get {
            call.respond(service.getAll())
            call.respondText { "Articulos AstroWiki" }
        }

        // GET /api/v1/articulos/{id}
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respondText("ID inválido", status = io.ktor.http.HttpStatusCode.BadRequest)

            val articulo = service.getById(id)
                ?: return@get call.respondText("Artículo no encontrado", status = io.ktor.http.HttpStatusCode.NotFound)

            call.respond(articulo)
        }

        // POST /api/v1/articulos
        post {
            val articulo = call.receive<Articulo>()
            val id = service.create(articulo)
            call.respond(mapOf("id_articulo" to id))
        }

        // PUT /api/v1/articulos/{id}
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respondText("ID inválido", status = io.ktor.http.HttpStatusCode.BadRequest)

            val articulo = call.receive<Articulo>()
            val updated = service.update(id, articulo)

            if (updated) call.respondText("Artículo actualizado")
            else call.respondText("No se pudo actualizar", status = io.ktor.http.HttpStatusCode.NotFound)
        }

        // DELETE /api/v1/articulos/{id}
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respondText("ID inválido", status = io.ktor.http.HttpStatusCode.BadRequest)

            val deleted = service.delete(id)
            if (deleted) call.respondText("Artículo marcado como borrado")
            else call.respondText("No encontrado", status = io.ktor.http.HttpStatusCode.NotFound)
        }
    }
}
