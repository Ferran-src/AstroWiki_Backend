package org.example.services


import at.favre.lib.crypto.bcrypt.BCrypt
import org.example.daos.UsuarioDAO
import org.example.models.Usuario

data class ActualizarPerfilRequest(
    val nombreUsuario: String,
    val correo: String,
    val rol: String? = null,
    val newImageBytes: ByteArray? = null,
    val newImageMimeType: String? = null,
    val newImageOriginalFileName: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActualizarPerfilRequest

        if (nombreUsuario != other.nombreUsuario) return false
        if (correo != other.correo) return false
        if (rol != other.rol) return false
        if (!newImageBytes.contentEquals(other.newImageBytes)) return false
        if (newImageMimeType != other.newImageMimeType) return false
        if (newImageOriginalFileName != other.newImageOriginalFileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nombreUsuario.hashCode()
        result = 31 * result + correo.hashCode()
        result = 31 * result + (rol?.hashCode() ?: 0)
        result = 31 * result + (newImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (newImageMimeType?.hashCode() ?: 0)
        result = 31 * result + (newImageOriginalFileName?.hashCode() ?: 0)
        return result
    }
}

data class CambiarContrasenaRequest(
    val nuevaContrasena: String,
    val contrasenaActual: String? = null
)
const val UPLOAD_DIR_PATH = "./uploads"

class UsuarioService {
    private val dao = UsuarioDAO
    private val imagenService = ImagenService(UPLOAD_DIR_PATH)

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
        val usuario =
            dao.findByCorreo(correo) ?: return null

        val passwordVerificationResult = BCrypt.verifyer().verify(
            contrasenaPlana.toCharArray(),
            usuario.contraseña // El hash almacenado
        )

        if (passwordVerificationResult.verified) {

            return usuario.copy(contraseña = "")
        } else {
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

    fun updatePerfilUsuario(id: Int, request: ActualizarPerfilRequest): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para actualización de perfil: $id")
        }
        validateNombreUsuarioAndCorreo(request.nombreUsuario, request.correo)

        val usuarioExistente = dao.findById(id) ?: return false

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

        var updatedImageUrl: String? = usuarioExistente.imagen
        var success = true

        // --- Manejo de la Imagen ---
        if (request.newImageBytes != null && request.newImageOriginalFileName != null) {
            if (usuarioExistente.imagen != null) {
                imagenService.deleteImageByRelativePath(usuarioExistente.imagen)
            }
            try {
                updatedImageUrl = imagenService.saveImageAndGetRelativePath(
                    request.newImageBytes,
                    request.newImageOriginalFileName,
                    request.newImageMimeType,
                    TipoEntidad.USUARIO
                )
            } catch (e: Exception) {
                updatedImageUrl = usuarioExistente.imagen
                success = false
            }
        } else if (request.newImageBytes == null && request.newImageOriginalFileName == null && usuarioExistente.imagen != null) {
            updatedImageUrl = usuarioExistente.imagen
        }



        val dbUpdateSuccess = dao.updateProfile(id, nombreUsuario =  request.nombreUsuario, correo =  request.correo,request.rol, updatedImageUrl)

        return success && dbUpdateSuccess
    }

    fun cambiarContrasenaUsuario(id: Int, request: CambiarContrasenaRequest): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para cambio de contraseña: $id")
        }
        if (request.contrasenaActual == null) throw IllegalArgumentException("Se requiere la contraseña actual")

        val hashedPassword = hashPassword(request.nuevaContrasena)

        return dao.updatePassword(id, hashedPassword)
    }



    fun deleteUsuario(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de usuario inválido para eliminación: $id")
        }
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