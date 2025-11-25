package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.models.DynamicQueryRequest

import org.example.services.DynamicQueryService

fun Route.dynamicQueryRoutes() {
    val service = DynamicQueryService()

    // Endpoint para solicitudes POST con cuerpo JSON
    post("/api/v1/query") {
        try {
            // Ktor deserializa automáticamente el cuerpo JSON en un objeto DynamicQueryRequest
            val requestData = call.receive<DynamicQueryRequest>()
            val results = service.executeQuery(requestData) // El servicio maneja la validación y la consulta
            call.respond(results)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno del servidor"))
            e.printStackTrace()
        }
    }


}