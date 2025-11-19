package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticuloCategoria(
    @SerialName("articulo_id") val articuloId: Int,
    @SerialName("categoria_id") val categoriaId: Int
)
