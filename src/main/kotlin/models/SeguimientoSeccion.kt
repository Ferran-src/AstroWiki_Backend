package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeguimientoSeccion(
    @SerialName("usuario_id") val usuarioId: Int,
    @SerialName("seccion_id") val seccionId: Int
)