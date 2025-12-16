package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Usuario
import org.example.services.ActualizarPerfilRequest
import org.example.services.CambiarContrasenaRequest
import org.example.services.UsuarioService
import org.example.models.DynamicQueryRequest
import org.example.services.DynamicQueryService

// Data class para recibir credenciales de login
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

// Data class para la respuesta de login (opcional, recomendable usar tokens)
data class LoginResponse(
    val message: String,
    val userId: Int? = null
    // val token: String? = null // <-- Idealmente devolverías un token JWT aquí
)

fun Route.usuarioRoutes() {
    val service = UsuarioService()
    val dynamicService = DynamicQueryService()

    route("/usuarios") {
        // --- Ruta para Consulta Dinámica ---
        post("/query") {
            try {
                val requestData = call.receive<DynamicQueryRequest>()
                val results = dynamicService.executeQuery(requestData)
                call.respond(results)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        // --- Ruta para Login ---
        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()
                val usuarioAutenticado = service.authenticateUsuario(loginRequest.correo, loginRequest.contrasena)

                if (usuarioAutenticado != null) {
                    // Autenticación exitosa
                    // Idealmente, aquí generas un token JWT y lo devuelves
                    // Por ahora, devolvemos un mensaje y el ID del usuario
                    call.respond(HttpStatusCode.OK, LoginResponse("Login exitoso", usuarioAutenticado.idUsuario))
                } else {
                    // Autenticación fallida
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }


        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val usuario = service.getUsuarioById(id)
                if (usuario != null) {

                    call.respond(usuario)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
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


        put("/{id}/perfil") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val perfilRequest = call.receive<ActualizarPerfilRequest>()
                    if (service.updatePerfilUsuario(id, perfilRequest)) {
                        call.respond(HttpStatusCode.OK, message ="usuario Actualizado correctamente")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Usuario no encontrado para actualizar perfil")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
            }
        }


        put("/{id}/contrasena") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                try {
                    val contrasenaRequest = call.receive<CambiarContrasenaRequest>()
                    if (service.cambiarContrasenaUsuario(id, contrasenaRequest)) {
                        call.respond(HttpStatusCode.OK, "Contraseña actualizada correctamente")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Usuario no encontrado para cambiar contraseña")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                    e.printStackTrace()
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
            }
        }


        delete("/{id}") {
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
    }
}