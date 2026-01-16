package org.example.routes

// routes/ComentariosRoutes.kt

import io.ktor.http.*
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Comentario
import org.example.services.ComentariosService


fun Route.comentarioRoutes() { // Recibe el 'Route' actual como contexto
    val service = ComentariosService() // Instancia del servicio

    route("/comentarios") {

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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                }
            }

            // Actualizar un comentario existente (puede incluir imagen)
            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        // Recibe la solicitud multipart
                        val multipartData = call.receive<MultiPartData>()

                        var contenido: String? = null
                        var autorId: Int? = null
                        var postId: Int? = null
                        var comentarioPadreId: Int? = null
                        var newImageBytes: ByteArray? = null
                        var newImageMimeType: String? = null
                        var newImageOriginalFileName: String? = null

                        multipartData.forEachPart { part ->
                            when (part) {
                                is PartData.FileItem -> {
                                    // Suponemos que el archivo de imagen se llama "imagen" en el formulario
                                    if (part.name == "imagen") {
                                        newImageOriginalFileName = part.originalFileName
                                        newImageMimeType = part.contentType?.toString()
                                        newImageBytes = part.streamProvider().use { it.readAllBytes() }
                                    }
                                }
                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "contenido" -> contenido = part.value
                                        "autorId" -> autorId = part.value.toIntOrNull()
                                        "postId" -> postId = part.value.toIntOrNull()
                                        "comentarioPadreId" -> comentarioPadreId = part.value.toIntOrNull()
                                        // Manejar otros campos del formulario si es necesario
                                    }
                                }
                                else -> {} // Ignorar otras partes si las hay
                            }
                            part.dispose() // Libera recursos
                        }

                        // Valida que se hayan recibido al menos los campos obligatorios
                        if (contenido == null || autorId == null || postId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Faltan campos obligatorios: contenido, autorId, postId"))
                            return@put
                        }

                        // Obtenemos el comentario existente para mantener otros campos como likeCount
                        val comentarioExistente = service.getComentarioById(id)
                        if (comentarioExistente == null) {
                            call.respond(HttpStatusCode.NotFound, "Comentario no encontrado")
                            return@put
                        }

                        // Crea el objeto Comentario actualizado, manteniendo campos como likeCount y fechaCreacion
                        val comentarioActualizado = Comentario(
                            idComentario = comentarioExistente.idComentario, // Mantener ID
                            contenido = contenido,
                            imagen = comentarioExistente.imagen, // Mantener imagen existente o se actualizará abajo
                            likeCount = comentarioExistente.likeCount, // Mantener contador de likes
                            autorId = autorId,
                            postId = postId,
                            comentarioPadreId = comentarioPadreId, // Puede ser null
                            fechaCreacion = comentarioExistente.fechaCreacion // Mantener fecha de creación
                        )

                        if (service.updateComentario(id, comentarioActualizado, newImageBytes, newImageMimeType, newImageOriginalFileName)) {
                            val comentarioActualizadoDesdeBD = service.getComentarioById(id) // Obtener el comentario actualizado de la BD para reflejar cambios de imagen o triggers
                            call.respond(HttpStatusCode.OK, comentarioActualizadoDesdeBD)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Comentario no encontrado para actualizar")
                        }

                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                }
            }

            // Eliminar un comentario
            delete {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        if (service.deleteComentario(id)) { // Llama al servicio
                            call.respond(HttpStatusCode.OK, "Comentario eliminado correctamente")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Comentario no encontrado para eliminar")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de comentario inválido")
                }
            }
        }

        // Crear un nuevo comentario (puede incluir imagen)
        post {
            try {
                // Recibe la solicitud multipart
                val multipartData = call.receive<MultiPartData>()

                var contenido: String? = null
                var autorId: Int? = null
                var postId: Int? = null
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
                                "autorId" -> autorId = part.value.toIntOrNull()
                                "postId" -> postId = part.value.toIntOrNull()
                                "comentarioPadreId" -> comentarioPadreId = part.value.toIntOrNull()
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                // Valida campos obligatorios
                if (contenido == null || autorId == null || postId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Faltan campos obligatorios: contenido, autorId, postId"))
                    return@post
                }

                // Crea el objeto Comentario
                val nuevoComentario = Comentario(
                    contenido = contenido,
                    imagen = null, // Se asignará en el service si se sube una imagen
                    likeCount = "0", // Valor inicial, la BD lo pondrá a 0, el service lo reflejará
                    autorId = autorId,
                    postId = postId,
                    comentarioPadreId = comentarioPadreId // Puede ser null
                    // fechaCreacion se asigna por defecto en la BD
                )

                val comentarioCreado = service.createComentario(nuevoComentario, newImageBytes, newImageMimeType, newImageOriginalFileName)
                call.respond(HttpStatusCode.Created, comentarioCreado)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        // Ruta para obtener comentarios de un post específico
        // GET /comentarios/post/{postId}
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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de autor inválido")
                }
            }
        }
    }
}