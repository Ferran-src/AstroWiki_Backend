package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Seccion(
    @SerialName("id_seccion") val idSeccion: Int? = null,
    val titulo: String,
    val descripcion: String?,
    @SerialName("creador_id") val creadorId: Int?,
    @SerialName("fecha_creacion") val fechaCreacion: String ,
    val imagen: String?
)