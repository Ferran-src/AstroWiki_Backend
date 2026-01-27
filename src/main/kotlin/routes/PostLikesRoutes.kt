package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.PostLikes
import org.example.services.PostLikesService

fun Route.postLikesRoutes() {

    val service = PostLikesService()

    route("/posts-likes") {

        post {
            try {
                val body = call.receive<PostLikes>()

                val liked = service.toggleLike(
                    postId = body.idPost,
                    usuarioId = body.idUsuario
                )

                call.respond(mapOf("liked" to liked))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error interno del servidor")
                )
                e.printStackTrace()
            }
        }


        route("/usuario/{usuarioId}") {
            get {
                try {
                    val usuarioId = call.parameters["usuarioId"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de usuario inválido")
                        )

                    val postsIds = service.getPostsLikedByUsuario(usuarioId)
                    call.respond(postsIds)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error interno del servidor")
                    )
                    e.printStackTrace()
                }
            }
        }


        route("/post/{postId}") {
            get {
                try {
                    val postId = call.parameters["postId"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de post inválido")
                        )

                    val usuariosIds = service.getUsuariosWhoLikedPost(postId)
                    call.respond(usuariosIds)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error interno del servidor")
                    )
                    e.printStackTrace()
                }
            }
        }
    }
}