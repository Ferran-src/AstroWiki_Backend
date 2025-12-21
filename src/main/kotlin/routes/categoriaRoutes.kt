package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.Categoria
import org.example.services.CategoriaService

fun Route.categoriaRoutes() {

    val service = CategoriaService()

    route("/categorias") {

        // Obtener todas las categorías
        get {
            val categorias = service.getAll()
            call.respond(categorias)
        }

        // Obtener una categoría por ID
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            val categoria = service.getById(id)
                ?: return@get call.respond(
                    status = HttpStatusCode.NotFound,
                    mapOf("error" to "Categoría no encontrada")
                )

            call.respond(categoria)
        }

        // Crear una nueva categoría
        post {
            try {
                val categoria = call.receive<Categoria>()
                val id = service.create(categoria)
                call.respond(
                    status = HttpStatusCode.Created,
                    mapOf("id_categoria" to id)
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al crear categoría: ${e.message}")
                )
            }
        }

        route("/categorias/update") {
            // Actualizar una categoría
            put("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "ID inválido")
                    )

                try {
                    val categoria = call.receive<Categoria>()
                    val ok = service.update(id, categoria)

                    if (ok) {
                        call.respond(
                            status = HttpStatusCode.OK,
                            mapOf("message" to "Categoría actualizada correctamente")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            mapOf("error" to "Categoría no encontrada")
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "Error al actualizar categoría: ${e.message}")
                    )
                }
            }
        }

        // Eliminar una categoría
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            val ok = service.delete(id)

            if (ok) {
                call.respond(
                    status = HttpStatusCode.OK,
                    mapOf("message" to "Categoría eliminada correctamente")
                )
            } else {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    mapOf("error" to "Categoría no encontrada")
                )
            }
        }
    }
}