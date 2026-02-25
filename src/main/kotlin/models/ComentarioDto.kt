package org.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComentarioDto (

        @SerialName("id_comentario") val idComentario: Int? = null,
        val contenido: String,
        val imagen: String?,
        @SerialName("contador_likes") val likeCount: String,
        @SerialName("autor_id") val autorId: Int,
        @SerialName("post_id") val postId: Int,
        @SerialName("comentario_padre_id") val comentarioPadreId: Int?,
        @SerialName("fecha_creacion") val fechaCreacion: String?,
        @SerialName("autor_nombre") val autorNombre: String? = null,
        @SerialName("autor_imagen") val autorImagen: String? = null
    )
