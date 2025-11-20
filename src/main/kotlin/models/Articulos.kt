package org.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Articulo(
    @SerialName("id_articulo") val idArticulo: Int? = null,
    val titulo: String,
    val contenido: String,
    @SerialName("fecha_creacion") val fechaCreacion: String, // o LocalDateTime
    @SerialName("fecha_ultima_edicion") val fechaUltimaEdicion: String, // o LocalDateTime
    val estado: String // "activo" o "borrado"
)