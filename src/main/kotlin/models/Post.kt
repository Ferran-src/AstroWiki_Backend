package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    @SerialName("id_post") val idPost: Int? = null,
    val titulo: String,
    val imagen: String?,
    val contenido: String,
    val like: Boolean,
    //La imagen se guarda como ruta en la tabla donde se necesite
    val imagen: String,
    @SerialName("autor_id") val autorId: Int, // Asumiendo no nulo
    @SerialName("seccion_id") val seccionId: Int, // Asumiendo no nulo
    @SerialName("fecha_creacion") val fechaCreacion: String // o LocalDateTime
)
