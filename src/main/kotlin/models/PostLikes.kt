package org.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostLikes(
    @SerialName("post_id") val idPost: Int,
    @SerialName("usuario_id") val idUsuario: Int
)