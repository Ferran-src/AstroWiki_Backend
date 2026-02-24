package org.example.models


import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Post(
    @SerialName("id_post") val idPost: Int? = null,
    val titulo: String,
    val imagen: String?,
    val contenido: String,
    @SerialName("contador_likes") val likeCount: Int,
    @SerialName("contador_comentario") val comentarioCount: Int,
    @SerialName("autor_id") val autorId: Int,
    @SerialName("seccion_id") val seccionId: Int,
    @SerialName("fecha_creacion") @Contextual val fechaCreacion: java.time.Instant?
)
