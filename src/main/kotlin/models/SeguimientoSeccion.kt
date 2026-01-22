package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID

@Serializable
data class SeguimientoSeccion(
    @SerialName("usuario_id") val usuarioId: Int,
    @SerialName("seccion_id") val seccionId: Int
)