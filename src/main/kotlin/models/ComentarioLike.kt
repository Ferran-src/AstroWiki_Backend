package org.example.models

import kotlinx.serialization.SerialName
import org.jetbrains.exposed.dao.id.EntityID

data class ComentarioLike(
    @SerialName("comentario_id") val comentarioid: EntityID<Int>,
    @SerialName("usuario_id") val usuarioid: EntityID<Int>
)
