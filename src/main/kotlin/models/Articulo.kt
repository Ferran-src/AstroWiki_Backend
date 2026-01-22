package org.example.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import java.time.Instant

@Serializable
data class Articulo @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id_articulo") val idArticulo: Int? = null,
    val titulo: String,
    val contenido: String,
    @SerialName("fecha_creacion")  @Contextual val fechaCreacion: Instant,
    @SerialName("fecha_ultima_edicion") @Contextual val fechaUltimaEdicion: Instant,
    val estado: String
)