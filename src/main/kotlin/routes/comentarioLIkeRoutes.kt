package org.example.routes



import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.services.ComentariosLikesService


fun Route.comentarioLikesRoutes() {
    val service = ComentariosLikesService()

    route("/comentarios-likes") {

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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de usuario o comentario inválido")
                }
            }

            post {
                val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                if (usuarioId != null && comentarioId != null) {
                    try {
                        val likeCreado = service.addLike(usuarioId, comentarioId)
                        call.respond(HttpStatusCode.Created, likeCreado)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de usuario o comentario inválido")
                }
            }

            delete {
                val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                if (usuarioId != null && comentarioId != null) {
                    try {
                        if (service.removeLike(usuarioId, comentarioId)) {
                            call.respond(HttpStatusCode.OK, "Like eliminado correctamente")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Like no encontrado para eliminar")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de usuario o comentario inválido")
                }
            }
        }

        route("/comentario/{comentarioId}") {
            get {
                val comentarioId = call.parameters["comentarioId"]?.toIntOrNull()
                if (comentarioId != null) {
                    try {
                        val likes = service.getLikesByComentarioId(comentarioId)
                        call.respond(likes)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    }catch (e: NotFoundException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                }
            }
        }

        route("/usuario/{usuarioId}") {
            get {
                val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                if (usuarioId != null) {
                    try {
                        val likes = service.getLikesByUsuarioId(usuarioId)
                        call.respond(likes)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: NotFoundException) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
                }
            }
        }

    }
}