package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.Seccion
import org.example.services.SeccionesService

fun Route.seccionRoutes() { // Recibe el 'Route' actual como contexto
    val service = SeccionesService() // Instancia del servicio

    route("/secciones") {

        // --- Rutas CRUD Estándar ---

        // Obtener todas las secciones con información del creador
        get {
            try {
                val secciones = service.getAllSeccionesWithCreator()
                call.respond(secciones) // Devuelve la lista de secciones
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }

        // Obtener una sección específica por ID con información del creador
        route("/{id}") {
            get {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        val seccion = service.getSeccionByIdWithCreator(id)
                        if (seccion != null) {
                            call.respond(seccion) // Devuelve la sección encontrada
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Sección no encontrada")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                }
            }

            // Crear una nueva sección (dentro de la ruta /{id} - quizás quieras moverlo a la raíz del bloque 'route("/secciones")')
            // post { ... } // <-- No es común tener POST aquí si el ID es para identificar el recurso a crear

            put {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id != null) {
                    try {
                        val seccionActualizada = call.receive<Seccion>()
                        if (service.updateSeccion(id, seccionActualizada)) {
                            call.respond(HttpStatusCode.OK, seccionActualizada)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Sección no encontrada para actualizar")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
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
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                        e.printStackTrace()
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "ID de sección inválido")
                }
            }
        }

        post {
            try {
                val nuevaSeccion = call.receive<Seccion>()
                val seccionCreada = service.createSeccion(nuevaSeccion)
                call.respond(HttpStatusCode.Created, seccionCreada)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
                e.printStackTrace()
            }
        }
    }
}