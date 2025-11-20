package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Imagen(
    @SerialName("id_imagen") val idImagen: Int? = null,
    val titulo: String?,
    val descripcion: String?,
    val url: String,
    @SerialName("fecha_subida") val fechaSubida: String, // o LocalDateTime
    @SerialName("articulo_id") val articuloId: Int? // Puede ser null si no está asociada a un artículo
)
