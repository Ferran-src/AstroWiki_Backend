package org.example.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@Serializable
data class Articulo @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id_articulo") val idArticulo: Int? = null,
    val titulo: String,
    val contenido: String,
    @SerialName("fecha_creacion") val fechaCreacion: LocalDateTime,
    @SerialName("fecha_ultima_edicion") val fechaUltimaEdicion: LocalDateTime,
    val estado: String
)