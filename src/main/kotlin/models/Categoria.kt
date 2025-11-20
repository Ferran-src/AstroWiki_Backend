package org.example.models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Categoria(
    @SerialName("id_categoria") val idCategoria: Int? = null,
    @SerialName("nombre_categoria") val nombreCategoria: String,
    val descripcion: String?
)
