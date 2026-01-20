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
            try {
                call.respond(service.getAll())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        route("/{id}") {
            // Obtener una categoría por ID
            get {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(status = HttpStatusCode.BadRequest, "ID inválido")

                    val categoria = service.getById(id)
                        ?: return@get call.respond(status = HttpStatusCode.NotFound, "No encontrado")

                    call.respond(categoria.toString())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            // Crear una nueva categoría
            post {
                try {
                    val categoria = call.receive<Categoria>()
                    val id = service.create(categoria)
                    call.respond(status = HttpStatusCode.Created,mapOf("id_categoria" to id)
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            // Actualizar una categoría
            put {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(status = HttpStatusCode.BadRequest,mapOf("error" to "ID inválido"))

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
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            // Eliminar una categoría
            delete("{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(
                            status = HttpStatusCode.BadRequest, mapOf("error" to "ID inválido")
                        )

                    val ok = service.delete(id)

                    if (ok) {
                        call.respond(
                            status = HttpStatusCode.OK, mapOf("message" to "Categoría eliminada correctamente")
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.NotFound, mapOf("error" to "Categoría no encontrada")
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.message))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Error interno del servidor"))
                }
            }
        }
    }
}