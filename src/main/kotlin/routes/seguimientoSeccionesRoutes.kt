package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.database.Secciones
import org.example.database.Usuarios
import org.example.models.Seccion
import org.example.models.SeguimientoSeccion
import org.example.services.SeguimientoSeccionesService
import org.jetbrains.exposed.dao.id.EntityID


fun Route.seguimientoSeccionesRoutes() {
    val service = SeguimientoSeccionesService()

    route("/seguimientos") {

        authenticate("auth-jwt") {

            get {
                try {
                    val seguimientos = service.getAllSeguimientos()
                    call.respond(seguimientos)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            route("/{usuarioId}/{seccionId}") {
                get {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                    if (usuarioId != null && seccionId != null) {
                        try {
                            val seguimiento = service.getSeguimientoByUsuarioAndSeccion(usuarioId, seccionId)
                            if (seguimiento != null) {
                                call.respond(seguimiento)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Seguimiento no encontrado")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario o sección inválido")
                    }
                }

                post {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                    if (usuarioId != null && seccionId != null) {
                        val usuarioEntityID = EntityID(usuarioId, Usuarios)
                        val seccionEntityID = EntityID(seccionId, Secciones)
                        try {
                            val seguimientoCreado = service.createSeguimiento(usuarioEntityID, seccionEntityID)
                            call.respond(HttpStatusCode.Created, seguimientoCreado)
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
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario o sección inválido")
                    }
                }

                // Crear un nuevo seguimiento (usuarioId, seccionId) - Opción B: Con cuerpo JSON
                // put { // O también post, aunque PUT aquí puede implicar "actualizar" la relación
                //     val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                //     val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                //     if (usuarioId != null && seccionId != null) {
                //         try {
                //             val seguimientoRequest = call.receive<SeguimientoSeccion>() // Recibe objeto, pero ignora los IDs del cuerpo
                //             // Opcional: Validar que los IDs del cuerpo coincidan con los de la URL
                //             if (seguimientoRequest.usuarioId != usuarioId || seguimientoRequest.seccionId != seccionId) {
                //                 call.respond(HttpStatusCode.BadRequest, "Los IDs del cuerpo no coinciden con los de la URL.")
                //                 return@put
                //             }
                //             val seguimientoCreado = service.createSeguimientoConObjeto(seguimientoRequest)
                //             call.respond(HttpStatusCode.Created, seguimientoCreado)
                //         } catch (e: IllegalArgumentException) {
                //             call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                //         } catch (e: Exception) {
                //             call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                //             e.printStackTrace()
                //         }
                //     } else {
                //         call.respond(HttpStatusCode.BadRequest, "ID de usuario o sección inválido")
                //     }
                // }

                delete {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                    if (usuarioId != null && seccionId != null) {
                        try {
                            if (service.deleteSeguimiento(usuarioId, seccionId)) {
                                call.respond(HttpStatusCode.OK, "Seguimiento eliminado correctamente")
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Seguimiento no encontrado para eliminar")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario o sección inválido")
                    }
                }
            }

            route("/usuario/{usuarioId}") {
                get {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    if (usuarioId != null) {
                        try {
                            val seguimientos = service.getSeguimientosByUsuarioId(usuarioId)
                            call.respond(seguimientos)
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
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
                    }
                }
            }

            route("/seccion/{seccionId}") {
                get {
                    val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                    if (seccionId != null) {
                        try {
                            val seguimientos = service.getSeguimientosBySeccionId(seccionId)
                            call.respond(seguimientos)
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
                    val nuevoSeguimiento = call.receive<SeguimientoSeccion>()
                    val seguimientoCreado = service.createSeguimientoConObjeto(nuevoSeguimiento)
                    call.respond(HttpStatusCode.Created, seguimientoCreado)
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