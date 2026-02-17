package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Seccion
import org.example.services.SeccionesService

fun Route.seccionRoutes() {
    val service = SeccionesService()

    route("/secciones") {

        authenticate("auth-jwt") {

            get {
                try {
                    val secciones = service.getAllSeccionesWithCreator()

                    call.respond(secciones)
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
                                call.respond(seccion)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Sección no encontrada")
                            }
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Error interno del servidor")
                            )
                            e.printStackTrace()
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                    }
                }


                put {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        try {
                            val seccionActualizada = call.receive<Seccion>()
                            if (service.updateSeccion(id, seccionActualizada)) {
                                call.respond(HttpStatusCode.OK, seccionActualizada)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Sección no encontrada para actualizar")
                            }
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Error interno del servidor")
                            )
                            e.printStackTrace()
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                    }
                }

                delete {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        try {
                            if (service.deleteSeccion(id)) {
                                call.respond(HttpStatusCode.OK, "Sección eliminada correctamente")
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Sección no encontrada para eliminar")
                            }
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        } catch (e: Exception) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Error interno del servidor")
                            )
                            e.printStackTrace()
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                    }
                }
            }

            post {
                try {
                    val nuevaSeccion = call.receive<Seccion>()
                    val seccionCreada = service.createSeccion(nuevaSeccion)
                    call.respond(HttpStatusCode.Created, seccionCreada)
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