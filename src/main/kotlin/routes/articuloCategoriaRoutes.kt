package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.models.ArticuloCategoria
import org.example.services.ArticuloCategoriaService

fun Route.articuloCategoriaRoutes() {

    val service = ArticuloCategoriaService()

    route("/articulos-categorias") {

        // Obtener todas las relaciones
        get {
            val relaciones = service.getAll()
            call.respond(relaciones)
        }

        // Obtener relaciones por artículo ID
        get("/articulo/{articuloId}") {
            val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de artículo inválido")
                )

            val relaciones = service.getByArticuloId(articuloId)
            call.respond(relaciones)
        }

        // Obtener relaciones por categoría ID
        get("/categoria/{categoriaId}") {
            val categoriaId = call.parameters["categoriaId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de categoría inválido")
                )

            val relaciones = service.getByCategoriaId(categoriaId)
            call.respond(relaciones)
        }

        // Verificar si existe una relación específica
        get("/exists") {
            val articuloId = call.request.queryParameters["articuloId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Falta articuloId")
                )

            val categoriaId = call.request.queryParameters["categoriaId"]?.toIntOrNull()
                ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Falta categoriaId")
                )

            val exists = service.exists(articuloId, categoriaId)
            call.respond(mapOf("exists" to exists))
        }

        // Crear nueva relación (asignar categoría a artículo)
        post {
            try {
                val relacion = call.receive<ArticuloCategoria>()
                val success = service.create(relacion)

                if (success) {
                    call.respond(
                        status = HttpStatusCode.Created,
                        mapOf("message" to "Relación creada correctamente")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.Conflict,
                        mapOf("error" to "La relación ya existe o hay un error con las claves foráneas")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al crear relación: ${e.message}")
                )
            }
        }

        // Crear múltiples relaciones (asignar varias categorías a un artículo)
        post("/batch") {
            try {
                val relaciones = call.receive<List<ArticuloCategoria>>()
                val createdCount = service.createBatch(relaciones)

                call.respond(
                    status = HttpStatusCode.Created,
                    mapOf(
                        "message" to "Relaciones creadas correctamente",
                        "created" to createdCount,
                        "total" to relaciones.size
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al crear relaciones: ${e.message}")
                )
            }
        }

        // Eliminar una relación específica (quitar categoría de artículo)
        delete("/articulo/{articuloId}/categoria/{categoriaId}") {
            val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de artículo inválido")
                )

            val categoriaId = call.parameters["categoriaId"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de categoría inválido")
                )

            val ok = service.delete(articuloId, categoriaId)

            if (ok) {
                call.respond(
                    status = HttpStatusCode.OK,
                    mapOf("message" to "Relación eliminada correctamente")
                )
            } else {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    mapOf("error" to "Relación no encontrada")
                )
            }
        }

        // Eliminar todas las categorías de un artículo
        delete("/articulo/{articuloId}") {
            val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de artículo inválido")
                )

            val deletedCount = service.deleteByArticuloId(articuloId)

            call.respond(
                status = HttpStatusCode.OK,
                mapOf(
                    "message" to "Relaciones eliminadas correctamente",
                    "deleted" to deletedCount
                )
            )
        }

        // Eliminar todos los artículos de una categoría
        delete("/categoria/{categoriaId}") {
            val categoriaId = call.parameters["categoriaId"]?.toIntOrNull()
                ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de categoría inválido")
                )

            val deletedCount = service.deleteByCategoriaId(categoriaId)

            call.respond(
                status = HttpStatusCode.OK,
                mapOf(
                    "message" to "Relaciones eliminadas correctamente",
                    "deleted" to deletedCount
                )
            )
        }

        // Reemplazar todas las categorías de un artículo (útil para actualización)
        put("/articulo/{articuloId}") {
            val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "ID de artículo inválido")
                )

            try {
                val categoriasIds = call.receive<List<Int>>()
                val result = service.replaceCategoriasForArticulo(articuloId, categoriasIds)

                call.respond(
                    status = HttpStatusCode.OK,
                    mapOf(
                        "message" to "Categorías actualizadas correctamente",
                        "articuloId" to articuloId,
                        "oldCategoriasCount" to result.first,
                        "newCategoriasCount" to result.second
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    mapOf("error" to "Error al actualizar categorías: ${e.message}")
                )
            }
        }
    }
}