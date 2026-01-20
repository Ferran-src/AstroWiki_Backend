package org.example.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id_usuario") val idUsuario: Int? = null,
    @SerialName("nombre_usuario") val nombreUsuario: String,
    val correo: String,
    val contraseña: String,
    @SerialName("fecha_registro") val fechaRegistro: String,
    val rol: String,
    val imagen: String?
)