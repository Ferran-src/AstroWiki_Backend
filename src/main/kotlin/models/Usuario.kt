package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id_usuario") val idUsuario: Int? = null, // Puede ser null si se genera en BD
    @SerialName("nombre_usuario") val nombreUsuario: String,
    val correo: String,
    val contraseña: String, // Cuidado con serializar contraseñas en respuestas de API
    @SerialName("fecha_registro") val fechaRegistro: String, // o LocalDateTime
    val rol: String // o un enum class ROL si se define
)