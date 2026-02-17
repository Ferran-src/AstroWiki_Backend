package org.example.routes

import io.ktor.http.*
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Post
import org.example.services.PostService
import org.jetbrains.exposed.dao.id.EntityID

fun Route.postRoutes() {

    val service = PostService()

    route("/posts") {

        authenticate("auth-jwt") {

            // GET /posts
            get {
                try {
                    call.respond(service.getAll())
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error interno del servidor")
                    )
                    e.printStackTrace()
                }
            }

            // create post
            post {
                try {
                    val multipartData = call.receive<MultiPartData>()

                    var titulo: String? = null
                    var contenido: String? = null
                    var autorId: EntityID<Int>? = null
                    var seccionId: EntityID<Int>? = null
                    var fechaCreacion: String? = null

                    var newImageBytes: ByteArray? = null
                    var newImageMimeType: String? = null
                    var newImageOriginalFileName: String? = null

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                if (part.name == "imagen") {
                                    newImageOriginalFileName = part.originalFileName
                                    newImageMimeType = part.contentType?.toString()
                                    newImageBytes = part.streamProvider().use { it.readAllBytes() }
                                }
                            }

                            is PartData.FormItem -> {
                                when (part.name) {
                                    "titulo" -> titulo = part.value
                                    "contenido" -> contenido = part.value
                                    "autorId" -> autorId = part.value.toIntOrNull() as EntityID<Int>?
                                    "seccionId" -> seccionId = part.value.toIntOrNull() as EntityID<Int>?
                                    "fechaCreacion" -> fechaCreacion = part.value
                                }
                            }

                            else -> {}
                        }
                        part.dispose()
                    }

                    if (titulo.isNullOrBlank() || contenido.isNullOrBlank() || autorId == null || seccionId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Faltan campos obligatorios: titulo, contenido, autorId, seccionId")
                        )
                        return@post
                    }

                    val nuevoPost = Post(
                        titulo = titulo,
                        contenido = contenido,
                        imagen = null,
                        likeCount = 0,
                        comentarioCount = 0,
                        autorId = autorId!!.value,
                        seccionId = seccionId!!.value,
                        fechaCreacion = java.time.Instant.now()
                    )

                    val id = service.create(
                        nuevoPost,
                        newImageBytes,
                        newImageMimeType,
                        newImageOriginalFileName
                    )

                    call.respond(
                        HttpStatusCode.Created,
                        mapOf("id_post" to id)
                    )

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            route("/{id}") {

                // GET posts por id
                get {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de post inválido")
                        )

                    try {
                        val post = service.getById(id)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Post no encontrado")
                            )

                        call.respond(post)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }

                // update post
                put {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de post inválido")
                        )

                    try {
                        val multipartData = call.receive<MultiPartData>()

                        var titulo: String? = null
                        var contenido: String? = null
                        var autorId: Int? = null
                        var seccionId: Int? = null
                        var fechaCreacion: String? = null

                        var newImageBytes: ByteArray? = null
                        var newImageMimeType: String? = null
                        var newImageOriginalFileName: String? = null

                        multipartData.forEachPart { part ->
                            when (part) {
                                is PartData.FileItem -> {
                                    if (part.name == "imagen") {
                                        newImageOriginalFileName = part.originalFileName
                                        newImageMimeType = part.contentType?.toString()
                                        newImageBytes = part.streamProvider().use { it.readAllBytes() }
                                    }
                                }

                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "titulo" -> titulo = part.value
                                        "contenido" -> contenido = part.value
                                        "autorId" -> autorId = part.value.toIntOrNull()
                                        "seccionId" -> seccionId = part.value.toIntOrNull()
                                        "fechaCreacion" -> fechaCreacion = part.value
                                    }
                                }

                                else -> {}
                            }
                            part.dispose()
                        }

                        if (
                            titulo.isNullOrBlank() ||
                            contenido.isNullOrBlank() ||
                            autorId == null ||
                            seccionId == null
                        ) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Faltan campos obligatorios: titulo, contenido, autorId, seccionId")
                            )
                            return@put
                        }

                        val postExistente = service.getById(id)
                            ?: return@put call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Post no encontrado")
                            )

                        val postActualizado = Post(
                            idPost = postExistente.idPost,
                            titulo = titulo,
                            contenido = contenido,
                            imagen = postExistente.imagen, // se reemplaza en el service si llega nueva
                            likeCount = postExistente.likeCount,
                            comentarioCount = postExistente.comentarioCount,
                            autorId = autorId!!,
                            seccionId = seccionId!!,
                            fechaCreacion = postExistente.fechaCreacion
                        )

                        val ok = service.update(
                            id,
                            postActualizado,
                            newImageBytes,
                            newImageMimeType,
                            newImageOriginalFileName
                        )

                        if (ok) {
                            val actualizado = service.getById(id)
                            call.respond(HttpStatusCode.OK, actualizado!!)
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post no encontrado"))
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
                }

                // DELETE post
                delete {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de post inválido")
                        )

                    try {
                        val deleted = service.delete(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Post eliminado correctamente"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Post no encontrado"))
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }
            }

            // GET /posts/autor/{autorId}
            route("/autor/{autorId}") {
                get {
                    val autorId = call.parameters["autorId"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de autor inválido")
                        )

                    try {
                        call.respond(service.getByAutorId(autorId))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }
            }

            // GET /posts/seccion/{seccionId}
            route("/seccion/{seccionId}") {
                get {
                    val seccionId = call.parameters["seccionId"]?.toIntOrNull()
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "ID de sección inválido")
                        )

                    try {
                        call.respond(service.getBySeccionId(seccionId))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                }
            }

            // GET /posts/buscar?q=...
            get("/buscar") {
                val query = call.request.queryParameters["q"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Parámetro 'q' requerido")
                    )

                try {
                    call.respond(service.searchByTitulo(query))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            // GET /posts/recientes
            get("/recientes") {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                try {
                    call.respond(service.getRecentPosts(limit, offset))
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
