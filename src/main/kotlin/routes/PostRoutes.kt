package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.Post
import org.example.services.PostService

fun Route.postRoutes() {

    val service = PostService()

    route("/posts") {

        // Obtener todos los posts
        get {
            try {
                val posts = service.getAll()
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener posts: ${e.message}")
                )
            }
        }

        // Obtener un post por ID
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            try {
                val post = service.getById(id)
                    ?: return@get call.respond(
                        status = HttpStatusCode.NotFound,
                        mapOf("error" to "Post no encontrado")
                    )

                call.respond(post)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener post: ${e.message}")
                )
            }
        }

        get("/autor/{autorId}") {
            val autorId = call.parameters["autorId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de autor inválido")
                )

            try {
                val posts = service.getByAutorId(autorId)
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener posts por autor: ${e.message}")
                )
            }
        }

        // Obtener posts por sección
        get("/seccion/{seccionId}") {
            val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de sección inválido")
                )

            try {
                val posts = service.getBySeccionId(seccionId)
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener posts por sección: ${e.message}")
                )
            }
        }

        // Buscar posts por título (búsqueda parcial)
        get("/buscar") {
            val query = call.request.queryParameters["q"]
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Parámetro de búsqueda 'q' requerido")
                )

            try {
                val posts = service.searchByTitulo(query)
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error en búsqueda: ${e.message}")
                )
            }
        }

        // Crear un nuevo post
        post {
            try {
                val post = call.receive<Post>()

                // Validar campos requeridos
                if (post.titulo.isBlank()) {
                    return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "El título es requerido")
                    )
                }
                if (post.contenido.isBlank()) {
                    return@post call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "El contenido es requerido")
                    )
                }

                val id = service.create(post)
                call.respond(
                    status = HttpStatusCode.Created,
                    mapOf("id_post" to id)
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al crear post: ${e.message}")
                )
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            try {
                val post = call.receive<Post>()

                // Validar campos requeridos
                if (post.titulo.isBlank()) {
                    return@put call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "El título es requerido")
                    )
                }
                if (post.contenido.isBlank()) {
                    return@put call.respond(
                        status = HttpStatusCode.BadRequest,
                        mapOf("error" to "El contenido es requerido")
                    )
                }

                val ok = service.update(id, post)

                if (ok) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        mapOf("message" to "Post actualizado correctamente")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        mapOf("error" to "Post no encontrado")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al actualizar post: ${e.message}")
                )
            }
        }

        // Actualizar like de un post
        patch("{id}/like") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            try {
                val likeData = call.receive<Map<String, Boolean>>()
                val like = likeData["like"] ?: return@patch call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Campo 'like' requerido")
                )

                val ok = service.updateLike(id, like)

                if (ok) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        mapOf("message" to "Like actualizado correctamente")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        mapOf("error" to "Post no encontrado")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al actualizar like: ${e.message}")
                )
            }
        }

        // Eliminar un post
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )

            try {
                val ok = service.delete(id)

                if (ok) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        mapOf("message" to "Post eliminado correctamente")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        mapOf("error" to "Post no encontrado")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al eliminar post: ${e.message}")
                )
            }
        }

        // Obtener posts más recientes (paginación opcional)
        get("/recientes") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            try {
                val posts = service.getRecentPosts(limit, offset)
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener posts recientes: ${e.message}")
                )
            }
        }

        // Obtener posts con like
        get("/likes") {
            try {
                val posts = service.getPostsWithLikes()
                call.respond(posts)
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener posts con likes: ${e.message}")
                )
            }
        }
    }
}