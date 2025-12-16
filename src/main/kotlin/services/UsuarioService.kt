package org.example.services


import at.favre.lib.crypto.bcrypt.BCrypt
import org.example.daos.UsuarioDAO
import org.example.models.Usuario
import java.security.MessageDigest

data class ActualizarPerfilRequest(
    val nombreUsuario: String,
    val correo: String,
    val rol: String? = null
)

// Data class para recibir datos de cambio de contraseña
data class CambiarContrasenaRequest(
    val nuevaContrasena: String,
    val contrasenaActual: String? = null
)

class UsuarioService {
    private val dao = UsuarioDAO

    fun getUsuarioById(id: Int): Usuario? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $id")
        }
        return dao.findById(id)
    }

    fun getUsuarioByNombreUsuario(nombreUsuario: String): Usuario? {
        if (nombreUsuario.isBlank()) {
            throw IllegalArgumentException("Nombre de usuario no puede estar vacío")
        }
        return dao.findByNombreUsuario(nombreUsuario)
    }

    fun getUsuarioByCorreo(correo: String): Usuario? {
        if (!isValidEmail(correo)) {
            throw IllegalArgumentException("Correo electrónico inválido: $correo")
        }
        return dao.findByCorreo(correo)
    }

    fun authenticateUsuario(correo: String, contrasenaPlana: String): Usuario? {
        // 1. Buscar al usuario por correo
        val usuario =
            dao.findByCorreo(correo) ?: return null

        // 2. Comparar la contraseña ingresada con el hash almacenado
        // Asumiendo que la contraseña almacenada en la base de datos es un hash BCrypt.
        // El método BCrypt.verify() compara el texto plano con el hash.
        val passwordVerificationResult = BCrypt.verifyer().verify(
            contrasenaPlana.toCharArray(),
            usuario.contraseña // El hash almacenado
        )

        if (passwordVerificationResult.verified) {
            // Las contraseñas coinciden, autenticación exitosa.
            // Devolvemos el usuario (idealmente sin la contraseña en la respuesta).
            // Podrías devolver solo el ID o un objeto específico para autenticación (como un token).
            // Por simplicidad del ejemplo, devolvemos el objeto Usuario completo (menos la contraseña).
            // En la práctica, excluyes la contraseña del objeto devuelto o usas un DTO diferente.
            return usuario.copy(contraseña = "") // O no incluir la contraseña en el modelo de respuesta
        } else {
            // La contraseña no coincide.
            return null
        }
    }

    fun createUsuario(usuario: Usuario): Usuario {

        validateUsuario(usuario, isUpdate = false)

        if (dao.findByNombreUsuario(usuario.nombreUsuario) != null) {
            throw IllegalArgumentException("El nombre de usuario '${usuario.nombreUsuario}' ya está en uso.")
        }
        if (dao.findByCorreo(usuario.correo) != null) {
            throw IllegalArgumentException("El correo '${usuario.correo}' ya está en uso.")
        }

        val hashedPassword = hashPassword(usuario.contraseña)
        val usuarioConHash = usuario.copy(contraseña = hashedPassword)

        return dao.create(usuarioConHash)
    }

    // Metodo para actualizar el perfil (sin contraseña)
    fun updatePerfilUsuario(id: Int, request: ActualizarPerfilRequest): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para actualización de perfil: $id")
        }
        validateNombreUsuarioAndCorreo(request.nombreUsuario, request.correo)

        val usuarioExistente = dao.findById(id) ?: return false // Usuario no encontrado

        // Verificar unicidad si cambia nombre de usuario o correo
        if (request.nombreUsuario != usuarioExistente.nombreUsuario) {
            if (dao.findByNombreUsuario(request.nombreUsuario) != null) {
                throw IllegalArgumentException("El nombre de usuario '${request.nombreUsuario}' ya está en uso.")
            }
        }
        if (request.correo != usuarioExistente.correo) {
            if (dao.findByCorreo(request.correo) != null) {
                throw IllegalArgumentException("El correo '${request.correo}' ya está en uso.")
            }
        }

        return dao.updateProfile(id, request.nombreUsuario, request.correo, request.rol)
    }

    // Metodo para cambiar la contraseña
    fun cambiarContrasenaUsuario(id: Int, request: CambiarContrasenaRequest): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para cambio de contraseña: $id")
        }
        if (request.contrasenaActual == null) throw IllegalArgumentException("Se requiere la contraseña actual")

        val hashedPassword = hashPassword(request.nuevaContrasena)

        // Actualizar solo la contraseña en el DAO
        return dao.updatePassword(id, hashedPassword)
    }



    fun deleteUsuario(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para eliminación: $id")
        }
        // Si tiene dependencias, decidir si se permite borrar o se marca como inactivo.
        return dao.delete(id)
    }

    private fun validateUsuario(usuario: Usuario, isUpdate: Boolean) {
        if (!isUpdate && usuario.nombreUsuario.isBlank()) {
            throw IllegalArgumentException("El nombre de usuario es obligatorio.")
        }
        if (!isUpdate && usuario.correo.isBlank()) {
            throw IllegalArgumentException("El correo electrónico es obligatorio.")
        }
        if (!isValidEmail(usuario.correo)) {
            throw IllegalArgumentException("El correo electrónico '${usuario.correo}' no es válido.")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    private fun validateNombreUsuarioAndCorreo(nombreUsuario: String, correo: String) {
        if (nombreUsuario.isBlank()) {
            throw IllegalArgumentException("El nombre de usuario es obligatorio.")
        }
        if (!isValidEmail(correo)) {
            throw IllegalArgumentException("El correo electrónico '${correo}' no es válido.")
        }
    }
    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(4, password.toCharArray())
    }
}