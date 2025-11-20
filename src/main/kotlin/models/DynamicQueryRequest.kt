package org.example.models


import kotlinx.serialization.Serializable

@Serializable
data class DynamicQueryRequest(
    val entity: String, // "articulo", "usuario", etc. (debe estar en una lista blanca)
    val select: List<String>? = null, // Campos a seleccionar, ej: ["titulo", "fechaCreacion"]
    val where: List<Filter>? = null,  // Lista de filtros
    val orderBy: OrderBy? = null,     // Criterio de ordenación
    val limit: Int? = null,           // Límite de resultados
    val offset: Int? = null           // Desplazamiento (para paginación)
)

@Serializable
data class Filter(
    val field: String, // Nombre del campo, ej: "categoriaId", "titulo"
    val operator: String, // "eq", "neq", "gt", "lt", "gte", "lte", "in", "like", etc. (
    val value: String // Valor para el filtro
)

@Serializable
data class OrderBy(
    val field: String, // Nombre del campo
    val direction: String // "asc", "desc" (lista blanca)
)