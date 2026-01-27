package org.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID

@Serializable
data class ComentarioLike(
    @SerialName("comentario_id") val comentarioid: Int,
    @SerialName("usuario_id") val usuarioid: Int
)
