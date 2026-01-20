package org.example.models

import kotlinx.serialization.SerialName

data class ComentarioLike(
    @SerialName("comentario_id") val comentarioid: Int,
    @SerialName("usuario_id") val usuarioid: Int
)
