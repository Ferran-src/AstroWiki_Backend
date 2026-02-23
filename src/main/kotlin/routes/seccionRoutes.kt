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
            post {
                try {
                    val multipartData = call.receive<MultiPartData>()

                    var titulo: String? = null
                    var descripcion: String? = null
                    var creadorId: Int? = null
                    // No necesitamos fechaCreacion aquí, el backend la generará

                    var newImageBytes: ByteArray? = null
                    var newImageMimeType: String? = null
                    var newImageOriginalFileName: String? = null

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                if (part.name == "imagen") { // Asumiendo que el campo del archivo se llama "imagen"
                                    newImageOriginalFileName = part.originalFileName
                                    newImageMimeType = part.contentType?.toString()
                                    newImageBytes = part.streamProvider().use { it.readAllBytes() }
                                }
                            }

                            is PartData.FormItem -> {
                                when (part.name) {
                                    "titulo" -> titulo = part.value
                                    "descripcion" -> descripcion = part.value // Puede ser nulo
                                    "creador_id" -> creadorId = part.value.toIntOrNull() // Asumiendo que se envía como "creador_id"
                                }
                            }

                            else -> {}
                        }
                        part.dispose()
                    }

                    // Validar campos obligatorios
                    if (titulo.isNullOrBlank() || creadorId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Faltan campos obligatorios: titulo, creador_id")
                        )
                        return@post
                    }


                    val nuevaSeccion = Seccion(
                        titulo = titulo,
                        descripcion = descripcion,
                        creadorId = creadorId!!,
                        fechaCreacion = null,
                        imagen = null
                    )

                    val id = service.createSeccion(
                        nuevaSeccion,
                        newImageBytes, // Pasa los bytes de la imagen
                        newImageMimeType, // Pasa el tipo MIME
                        newImageOriginalFileName // Pasa el nombre original del archivo
                    )

                    call.respond(
                        HttpStatusCode.Created,
                        mapOf("id_seccion" to id)
                    )

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
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
                            // Recibe la solicitud multipart
                            val multipartData = call.receive<MultiPartData>()

                            var titulo: String? = null
                            var descripcion: String? = null
                            var creadorId: Int? = null

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
                                            "descripcion" -> descripcion = part.value
                                            "creador_id" -> creadorId = part.value.toIntOrNull()
                                        }
                                    }

                                    else -> {

                                    }
                                }
                                part.dispose()
                            }


                            val seccionUpdates = Seccion(
                                idSeccion = id,
                                titulo = titulo ?: "",
                                descripcion = descripcion,
                                creadorId = creadorId,
                                fechaCreacion = null,
                                imagen = null
                            )


                            if (service.updateSeccion(id, seccionUpdates, newImageBytes, newImageMimeType, newImageOriginalFileName)) {

                                val seccionActualizada = service.getSeccionByIdWithCreator(id)
                                if (seccionActualizada != null) {
                                    call.respond(HttpStatusCode.OK, seccionActualizada)
                                } else {

                                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al recuperar la sección actualizada."))
                                }
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
        }
    }
}