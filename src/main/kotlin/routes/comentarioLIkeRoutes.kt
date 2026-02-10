package org.example.routes



import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.database.Comentarios
import org.example.database.Usuarios
import org.example.services.ComentariosLikesService
import org.jetbrains.exposed.dao.id.EntityID


fun Route.comentarioLikesRoutes() {
    val service = ComentariosLikesService()

    route("/comentarios-likes") {

        authenticate("auth-jwt") {

            get {
                try {
                    val likes = service.getAllLikes()
                    call.respond(likes)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            route("/{usuarioId}/{comentarioId}") {
                get {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                    if (usuarioId != null && comentarioId != null) {
                        try {
                            val like = service.getLikeByUsuarioAndComentario(usuarioId, comentarioId)
                            if (like != null) {
                                call.respond(like)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Like no encontrado")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario o comentario inválido")
                    }
                }

                post {
                    try {
                        val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                        val usuarioEntityId = EntityID(usuarioId!!, Usuarios)
                        val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                        val comentarioEntityId = EntityID(comentarioId!!, Comentarios)

                        val likeCreado = service.addLike(usuarioEntityId, comentarioEntityId)
                        call.respond(HttpStatusCode.Created, likeCreado)
                    } catch (e: NullPointerException) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Ni el usuario ni el comentario pueden ser null")
                        )
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }

                delete {
                    try {
                        val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                        val usuarioEntityId = EntityID(usuarioId!!, Usuarios)
                        val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                        val comentarioEntityId = EntityID(comentarioId!!, Comentarios)
                        if (service.removeLike(usuarioEntityId, comentarioEntityId)) {
                            call.respond(HttpStatusCode.OK, "Like eliminado correctamente")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Like no encontrado para eliminar")
                        }
                    } catch (e: NullPointerException) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Ni el usuario ni el comentario pueden ser null")
                        )
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }
            }

            route("/comentario/{comentarioId}") {
                get {
                    try {
                        val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                        val comentarioEntityID = EntityID(comentarioId!!, Comentarios)
                        val likes = service.getLikesByComentarioId(comentarioEntityID)
                        call.respond(likes)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: NotFoundException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }
            }


            route("/usuario/{usuarioId}") {
                get {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                    if (usuarioId != null) {
                        try {
                            val usuarioEntityID = EntityID(usuarioId, Usuarios)
                            val likes = service.getLikesByUsuarioId(usuarioEntityID)
                            call.respond(likes)
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                        } catch (e: NotFoundException) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
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
        }
    }
}