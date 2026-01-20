package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comentario(
    @SerialName("id_comentario") val idComentario: Int? = null,
    val contenido: String,
    val imagen: String?,
    @SerialName("contador_likes")val likeCount : String,
    @SerialName("autor_id") val autorId: Int, // Asumiendo no nulo
    @SerialName("post_id") val postId: Int, // Asumiendo no nulo
    @SerialName("comentario_padre_id") val comentarioPadreId: Int?, // Puede ser null si es comentario raíz
    @SerialName("fecha_creacion") val fechaCreacion: String // o LocalDateTime
)