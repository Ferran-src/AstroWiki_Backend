package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Seccion
import org.example.services.SeccionesService


fun Route.seccionRoutes() {
    val service = SeccionesService() // Instancia del servicio

    route("/secciones") {

        get {
            try {
                val secciones = service.getAllSeccionesWithCreator()
                call.respond(secciones) // Devuelve la lista de secciones
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        val seccion = service.getSeccionByIdWithCreator(id)
                        if (seccion != null) {
                            call.respond(seccion) // Devuelve la sección encontrada
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Sección no encontrada")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                }
            }

            // Actualizar una sección existente
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        val seccionActualizada = call.receive<Seccion>() // Recibe la sección actualizada del cuerpo
                        if (service.updateSeccion(id, seccionActualizada)) { // Llama al servicio
                            call.respond(HttpStatusCode.OK, seccionActualizada) // Devuelve la sección actualizada
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Sección no encontrada para actualizar")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                }
            }

            // Eliminar una sección
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        if (service.deleteSeccion(id)) { // Llama al servicio
                            call.respond(HttpStatusCode.OK, "Sección eliminada correctamente")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Sección no encontrada para eliminar")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                }
            }
        }

        post {
            try {
                val nuevaSeccion = call.receive<Seccion>() // Recibe la nueva sección del cuerpo
                val seccionCreada = service.createSeccion(nuevaSeccion) // Llama al servicio
                call.respond(HttpStatusCode.Created, seccionCreada) // Devuelve la sección creada
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }
    }
}