package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

const val UPLOAD_DIR_BASE_PATH = "./uploads"

fun Route.imagenRoutes() {

    authenticate("auth-jwt") {

        get("/{relativePath...}") {
            val relativePathSegments = call.parameters.getAll("relativePath")
            if (relativePathSegments.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Ruta de imagen no especificada")
                return@get
            }
            val relativePath = relativePathSegments.joinToString("/")

            try {
                val normalizedPath = File(relativePath).normalize().toString()
                if (normalizedPath.contains("..") || normalizedPath.startsWith("/")) {
                    call.respond(HttpStatusCode.BadRequest, "Ruta de archivo inválida")
                    return@get
                }

                val absolutePath = File(UPLOAD_DIR_BASE_PATH, normalizedPath).absolutePath
                val fileToServe = File(absolutePath)

                if (fileToServe.exists() && fileToServe.isFile) {
                    // 1. Calcular el Content-Type basado en la extensión
                    val contentType = when (fileToServe.extension.lowercase()) {
                        "jpg", "jpeg" -> ContentType.Image.JPEG
                        "png" -> ContentType.Image.PNG
                        "gif" -> ContentType.Image.GIF
                        else -> ContentType.Application.OctetStream
                    }

                    call.response.headers.append(HttpHeaders.ContentType, contentType.toString())

                    call.respondFile(fileToServe)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Imagen no encontrada")
                }
            } catch (e: SecurityException) {
                call.respond(HttpStatusCode.Forbidden, "Acceso denegado al archivo")
                e.printStackTrace()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al servir la imagen"))
                e.printStackTrace()
            }
        }
    }
}