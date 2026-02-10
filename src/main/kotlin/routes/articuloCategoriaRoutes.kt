package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.example.database.Articulos
import org.example.database.Categorias
import org.example.models.ArticuloCategoria
import org.example.services.ArticuloCategoriaService
import org.jetbrains.exposed.dao.id.EntityID

fun Route.articuloCategoriaRoutes() {

    val service = ArticuloCategoriaService()

    route("/articulos-categorias") {

        authenticate("auth-jwt") {

            //get all relations
            get {
                try {
                    call.respond(service.getAll())
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error interno del servidor")
                    )
                    e.printStackTrace()
                }
            }

            //create single relation
            post {
                try {
                    val relacion = call.receive<ArticuloCategoria>()
                    val success = service.create(relacion)

                    if (success) {
                        call.respond(
                            HttpStatusCode.Created,
                            mapOf("message" to "Relación creada correctamente")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            mapOf("error" to "La relación ya existe o hay un error con las claves foráneas")
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to e.message)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error interno del servidor")
                    )
                    e.printStackTrace()
                }
            }

            //create many relations
            route("/batch") {
                post {
                    try {
                        val relaciones = call.receive<List<ArticuloCategoria>>()
                        val createdCount = service.createBatch(relaciones)

                        call.respond(
                            HttpStatusCode.Created,
                            mapOf(
                                "message" to "Relaciones creadas correctamente",
                                "created" to createdCount,
                                "total" to relaciones.size
                            )
                        )
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
            }

            route("/{id}") {
                //verify relation existence
                get {
                    try {
                        val articuloId = (call.request.queryParameters["articuloId"]?.toIntOrNull())
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Falta articuloId")
                            )
                        val articuloEntityID = EntityID(articuloId, Articulos)
                        val categoriaId = call.request.queryParameters["categoriaId"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Falta categoriaId")
                            )
                        val categoriaEntityID = EntityID(categoriaId, Articulos)

                        val exists = service.exists(articuloEntityID, categoriaEntityID)
                        call.respond(mapOf("exists" to exists))
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to e.message)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Error interno del servidor")
                        )
                        e.printStackTrace()
                    }
                }

                //delete specific relation
                delete {
                    try {
                        val articuloId = call.request.queryParameters["articuloId"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de artículo inválido")
                            )

                        val categoriaId = call.request.queryParameters["categoriaId"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de categoría inválido")
                            )

                        val ok = service.delete(articuloId, categoriaId)

                        if (ok) {
                            call.respond(mapOf("message" to "Relación eliminada correctamente"))
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Relación no encontrada")
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
                }
            }


            route("/articulo/{articuloId}") {

                // get by articulo
                get {
                    try {
                        val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de artículo inválido")
                            )

                        call.respond(service.getByArticuloId(articuloId))
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to e.message)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Error interno del servidor")
                        )
                        e.printStackTrace()
                    }
                }

                // delete all categorias from articulo
                delete {
                    try {
                        val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de artículo inválido")
                            )
                        val articuloEntityID = EntityID(articuloId, Articulos)

                        val deletedCount = service.deleteByArticuloId(articuloEntityID)

                        call.respond(
                            mapOf(
                                "message" to "Relaciones eliminadas correctamente",
                                "deleted" to deletedCount
                            )
                        )
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

                // replace categorias
                put {
                    try {
                        val articuloId = call.parameters["articuloId"]?.toIntOrNull()
                            ?: return@put call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de artículo inválido")
                            )
                        val articuloEntityID = EntityID(articuloId, Articulos)

                        val categoriasIds = call.receive<List<Int>>()

                        val categoriasEntityID: MutableList<EntityID<Int>> = mutableListOf()
                        categoriasIds.forEach { categoriasEntityID.add(EntityID(it, Categorias)) }

                        val result = service.replaceCategoriasForArticulo(articuloEntityID, categoriasEntityID)

                        call.respond(
                            mapOf(
                                "message" to "Categorías actualizadas correctamente",
                                "articuloId" to articuloId,
                                "oldCategoriasCount" to result.first,
                                "newCategoriasCount" to result.second
                            )
                        )
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
            }


            route("/categoria/{categoriaId}") {

                // get by categoria
                get {
                    try {
                        val categoriaId = call.parameters["categoriaId"]?.toIntOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de categoría inválido")
                            )

                        call.respond(service.getByCategoriaId(categoriaId))
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to e.message)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Error interno del servidor")
                        )
                        e.printStackTrace()
                    }
                }

                // delete all articulos from categoria
                delete {
                    try {
                        val categoriaId = call.parameters["categoriaId"]?.toIntOrNull()
                            ?: return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "ID de categoría inválido")
                            )

                        val deletedCount = service.deleteByCategoriaId(categoriaId)

                        call.respond(
                            mapOf(
                                "message" to "Relaciones eliminadas correctamente",
                                "deleted" to deletedCount
                            )
                        )
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
            }
        }
    }
}