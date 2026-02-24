package org.example.routes
import io.ktor.http.*
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.models.Usuario
import org.example.plugins.generateJWTToken

import org.example.services.UsuarioService
import org.example.services.ActualizarPerfilRequest
import org.example.services.CambiarContrasenaRequest

// Data class para recibir credenciales de login
@Serializable
data class LoginRequest(
    val userName: String?, 
    val correo: String?,
    val contrasena: String
)

@Serializable
data class LoginResponse(
    val message: String,
    val userId: Int? = null,
    val token: String? = null
)


fun Route.usuarioRoutes() {
    val service = UsuarioService()

    route("/usuarios") {

        route("/login") {
            post {
                try {
                    val loginRequest = call.receive<LoginRequest>()
                    val usuarioAutenticado = service.authenticateUsuario(loginRequest.userName, loginRequest.correo, loginRequest.contrasena)

                    if (usuarioAutenticado != null) {
                        val token = generateJWTToken(usuarioAutenticado)
                        println("JWT GENERADO: $token")
                        call.respond(HttpStatusCode.OK, LoginResponse("Login exitoso", usuarioAutenticado.idUsuario, token))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            }
        }

        post {
            try {
                val nuevoUsuario = call.receive<Usuario>()
                val usuarioCreado = service.createUsuario(nuevoUsuario)
                call.respond(HttpStatusCode.Created, usuarioCreado)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        authenticate("auth-jwt") {
            route("/{id}") {
                get {
                    // Esta ruta ahora requiere autenticación JWT
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()

                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null && id == userId) { // Verifica que el usuario esté accediendo a su propio perfil
                        val usuario = service.getUsuarioById(id)
                        if (usuario != null) {
                            call.respond(usuario)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Acceso denegado")
                    }
                }

                delete {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id != null) {
                        if (service.deleteUsuario(id)) {
                            call.respond(HttpStatusCode.OK, "Usuario eliminado correctamente")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Usuario no encontrado para eliminar")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
                    }
                }

                route("/perfil") {
                    put {

                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            try {
                                // Recibe la solicitud multipart
                                val multipartData = call.receive<MultiPartData>()

                                var nombreUsuario: String? = null
                                var correo: String? = null
                                var rol: String? = null
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
                                                "nombreUsuario" -> nombreUsuario = part.value
                                                "correo" -> correo = part.value
                                                "rol" -> rol = part.value
                                            }
                                        }

                                        else -> {}
                                    }
                                    part.dispose()
                                }

                                if (nombreUsuario == null || correo == null) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        mapOf("error" to "Faltan campos obligatorios: nombreUsuario, correo")
                                    )
                                    return@put
                                }


                                val perfilRequest = ActualizarPerfilRequest(
                                    nombreUsuario = nombreUsuario,
                                    correo = correo,
                                    rol = rol,
                                    newImageBytes = newImageBytes,
                                    newImageMimeType = newImageMimeType,
                                    newImageOriginalFileName = newImageOriginalFileName
                                )

                                // Llama al servicio con el objeto ya construido
                                if (service.updatePerfilUsuario(id, perfilRequest)) {
                                    val usuarioActualizado = service.getUsuarioById(id)
                                    call.respond(HttpStatusCode.OK, usuarioActualizado as Usuario)
                                } else {
                                    call.respond(
                                        HttpStatusCode.NotFound,
                                        "Usuario no encontrado para actualizar perfil"
                                    )
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
                            call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
                        }
                    }
                }

                route("/contrasena") {
                    put {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            try {
                                val contrasenaRequest = call.receive<CambiarContrasenaRequest>()
                                if (service.cambiarContrasenaUsuario(id, contrasenaRequest)) {
                                    call.respond(HttpStatusCode.OK, "Contraseña actualizada correctamente")
                                } else {
                                    call.respond(
                                        HttpStatusCode.NotFound,
                                        "Usuario no encontrado para cambiar contraseña"
                                    )
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
                            call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
                        }
                    }
                }
            }
        }
    }
}
