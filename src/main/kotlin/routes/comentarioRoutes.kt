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
import org.example.models.Comentario
import org.example.services.ComentariosService
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

fun Route.comentarioRoutes() {
    val service = ComentariosService()

    route("/comentarios") {

        authenticate("auth-jwt") {

            get {
                try {
                    val comentarios = service.getAllComentarios()
                    call.respond(comentarios) // Devuelve la lista de comentarios
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            // Ruta para operaciones que dependen del ID del comentario
            route("/{id}") {
                // Obtener un comentario específico por ID
                get {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        try {
                            val comentario = service.getComentarioById(id)
                            if (comentario != null) {
                                call.respond(comentario) // Devuelve el comentario encontrado
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Comentario no encontrado")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                    }
                }

                put {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        try {
                            // Recibe la solicitud multipart
                            val multipartData = call.receive<MultiPartData>()

                            var contenido: String? = null
                            var autorId: EntityID<Int>? = null
                            var postId: EntityID<Int>? = null
                            var comentarioPadreId: Int? = null
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
                                            "contenido" -> contenido = part.value
                                            "autorId" -> autorId = part.value.toIntOrNull() as EntityID<Int>?
                                            "postId" -> postId = part.value.toIntOrNull() as EntityID<Int>?
                                            "comentarioPadreId" -> comentarioPadreId = part.value.toIntOrNull()

                                        }
                                    }

                                    else -> {}
                                }
                                part.dispose()
                            }


                            if (contenido == null || autorId == null || postId == null) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Faltan campos obligatorios: contenido, autorId, postId")
                                )
                                return@put
                            }


                            val comentarioExistente = service.getComentarioById(id)
                            if (comentarioExistente == null) {
                                call.respond(HttpStatusCode.NotFound, "Comentario no encontrado")
                                return@put
                            }

                            val comentarioActualizado = Comentario(
                                idComentario = comentarioExistente.idComentario,
                                contenido = contenido,
                                imagen = comentarioExistente.imagen,
                                likeCount = comentarioExistente.likeCount,
                                autorId = autorId!!.value,
                                postId = postId!!.value,
                                comentarioPadreId = comentarioPadreId,
                                fechaCreacion = comentarioExistente.fechaCreacion
                            )

                            if (service.updateComentario(
                                    id,
                                    comentarioActualizado,
                                    newImageBytes,
                                    newImageMimeType,
                                    newImageOriginalFileName
                                )
                            ) {
                                val comentarioActualizadoDesdeBD = service.getComentarioById(id)
                                call.respond(HttpStatusCode.OK, comentarioActualizadoDesdeBD!!)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Comentario no encontrado para actualizar")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                    }
                }

                delete {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        try {
                            if (service.deleteComentario(id)) {
                                call.respond(HttpStatusCode.OK, "Comentario eliminado correctamente")
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Comentario no encontrado para eliminar")
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
                        call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                    }
                }
            }

            post {
                try {
                    val multipartData = call.receive<MultiPartData>()

                    var contenido: String? = null
                    var autorId: EntityID<Int>? = null
                    var postId: EntityID<Int>? = null
                    var comentarioPadreId: Int? = null
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
                                    "contenido" -> contenido = part.value
                                    "autorId" -> autorId = part.value.toIntOrNull() as EntityID<Int>?
                                    "postId" -> postId = part.value.toIntOrNull() as EntityID<Int>?
                                    "comentarioPadreId" -> comentarioPadreId = part.value.toIntOrNull()
                                    "fechaCreacion" -> fechaCreacion = part.value
                                }
                            }

                            else -> {}
                        }
                        part.dispose()
                    }


                    if (autorId == null || postId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Faltan campos obligatorios: autorId, postId")
                        )
                        return@post
                    }


                    val nuevoComentario = Comentario(
                        contenido = contenido!!,
                        imagen = null,
                        likeCount = "0",
                        autorId = autorId!!.value,
                        postId = postId!!.value,
                        comentarioPadreId = comentarioPadreId,
                        fechaCreacion = fechaCreacion ?: LocalDateTime.now().toString(),


                        )

                    val comentarioCreado = service.createComentario(
                        nuevoComentario,
                        newImageBytes,
                        newImageMimeType,
                        newImageOriginalFileName
                    )
                    call.respond(HttpStatusCode.Created, comentarioCreado)

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }

            route("/post/{postId}") {
                get {
                    val postId = call.parameters["postId"]?.toIntOrNull()
                    if (postId != null) {
                        try {
                            val comentarios = service.getComentariosByPostId(postId)
                            call.respond(comentarios)
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
                        call.respond(HttpStatusCode.BadRequest, "ID de post inválido")
                    }
                }
            }

            // Ruta para obtener comentarios de un autor específico
            // GET /comentarios/autor/{autorId}
            route("/autor/{autorId}") {
                get {
                    val autorId = call.parameters["autorId"]?.toIntOrNull()
                    if (autorId != null) {
                        try {
                            val comentarios = service.getComentariosByAutorId(autorId)
                            call.respond(comentarios)
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
                        call.respond(HttpStatusCode.BadRequest, "ID de autor inválido")
                    }
                }
            }
        }
    }
}