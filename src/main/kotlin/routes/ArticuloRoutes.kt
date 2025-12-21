package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.Articulo
import org.example.services.ArticuloService

fun Route.articuloRoutes() {

    val service = ArticuloService()

    route("/articulos") {

        get {
            call.respond(service.getAll())
            //call.respondText("Hola")
//            println("🔥 Entrando a GET /articulos")
//            val lista = service.getAll()
//            println("🔥 Resultado: $lista")
//            call.respondText(lista.toString())
        }

        //Functional get method. Redundatn because of Dynamic query
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(status = HttpStatusCode.BadRequest, "ID inválido")

            val articulo = service.getById(id)
                ?: return@get call.respond(status = HttpStatusCode.NotFound, "No encontrado")

            call.respond(articulo.toString())
        }

        post {
            val articulo = call.receive<Articulo>()
            val id = service.create(articulo)
            call.respond(mapOf("id" to id))
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "ID inválido")

            val articulo = call.receive<Articulo>()
            val ok = service.update(id, articulo)

            if (ok) call.respond("Actualizado")
            else call.respond(HttpStatusCode.NotFound, "No encontrado")
        }

        delete("/delete/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "ID inválido")

            val ok = service.delete(id)

            if (ok) call.respond("Marcado como borrado")
            else call.respond(HttpStatusCode.NotFound, "No encontrado")
        }
    }
}
