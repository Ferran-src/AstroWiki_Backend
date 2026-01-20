package org.example.models


import kotlinx.serialization.Serializable

@Serializable
data class DynamicQueryRequest(
    val entity: String,
    val select: List<String>? = null, // Campos a seleccionar, ej: ["titulo", "fechaCreacion"]
    val where: List<Filter>? = null,  // Lista de filtros
    val orderBy: OrderBy? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

@Serializable
data class Filter(
    val field: String,
    val operator: String,
    val value: String
)

@Serializable
data class OrderBy(
    val field: String,
    val direction: String
)