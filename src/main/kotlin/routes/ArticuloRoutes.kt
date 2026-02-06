package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.Articulo
import org.example.services.ArticuloService

fun Route.articuloRoutes() {

    val service = ArticuloService()

    route("/articulos") {

        authenticate("auth-jwt") {
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
        }
        post {
            try {
                val articulo = call.receive<Articulo>()
                val id = service.create(articulo)
                call.respond(mapOf("id" to id))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.message))
            }catch (e: org.postgresql.util.PSQLException){
                call.respond(HttpStatusCode.BadRequest, mapOf("Error" to "el estado tiene que ser un valor valido"))
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }
        route("/{id}") {
            get {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(status = HttpStatusCode.BadRequest, "ID inválido")

                    val articulo = service.getById(id)
                        ?: return@get call.respond(status = HttpStatusCode.NotFound, "No encontrado")

                    call.respond(articulo.toString())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }
            put {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "ID inválido")

                    val articulo = call.receive<Articulo>()
                    val ok = service.update(id, articulo)

                    if (ok) call.respond("Actualizado")
                    else call.respond(HttpStatusCode.NotFound, "No encontrado")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("Error" to e.message))
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            delete {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "ID inválido")

                    val ok = service.delete(id)

                    if (ok) call.respond("Marcado como borrado")
                    else call.respond(HttpStatusCode.NotFound, "No encontrado")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }
        }
    }
}
