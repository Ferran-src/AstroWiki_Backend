package org.example.models


import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.time.ExperimentalTime

@Serializable
data class Seccion@OptIn(ExperimentalTime::class) constructor(
    @SerialName("id_seccion") val idSeccion: Int? = null,
    val titulo: String,
    val descripcion: String?,
    @SerialName("creador_id") val creadorId: Int?,
    @SerialName("fecha_creacion") @Contextual val fechaCreacion: Instant?,
    val imagen: String?
)