package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Seccion(
    @SerialName("id_seccion") val idSeccion: Int? = null,
    val titulo: String,
    val descripcion: String?,
    @SerialName("creador_id") val creadorId: Int, // Puede ser null si no tiene creador fijo
    @SerialName("fecha_creacion") val fechaCreacion: String, // o LocalDateTime
    @SerialName("nombre_creador") val nombreCreador: String,
)