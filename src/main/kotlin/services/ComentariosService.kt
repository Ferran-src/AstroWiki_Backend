package org.example.services

import org.example.daos.ComentarioDAO
import org.example.models.Comentario
import org.example.daos.UsuarioDAO


class ComentariosService {
    private val dao = ComentarioDAO
    private val usuarioDao = UsuarioDAO
    private val imagenService = ImagenService(UPLOAD_DIR_PATH)

    fun getAllComentarios(): List<Comentario> {

        return dao.findAll()
    }

    fun getComentariosByPostId(postId: Int): List<Comentario> {
        if (postId <= 0) {
            throw IllegalArgumentException("ID de post inválido: $postId")
        }
        return dao.findByPostId(postId)
    }

    fun getComentariosByAutorId(autorId: Int): List<Comentario> {
        if (autorId <= 0) {
            throw IllegalArgumentException("ID de autor inválido: $autorId")
        }
        return dao.findByAutorId(autorId)
    }

    fun getComentarioById(id: Int): Comentario? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de comentario inválido: $id")
        }
        // Delegar al DAO
        return dao.findById(id)
    }

    fun createComentario(comentario: Comentario, newImageBytes: ByteArray? = null, newImageMimeType: String? = null, newImageOriginalFileName: String? = null): Comentario {

        validateComentario(comentario, isUpdate = false)

        val autor = usuarioDao.findById(comentario.autorId)
            ?: throw IllegalArgumentException("El usuario autor con ID ${comentario.autorId} no existe.")

        // 3. Verificar si el post existe
        // val post = postDao.findById(comentario.postId)
        // if (post == null) {
        //     throw IllegalArgumentException("El post con ID ${comentario.postId} no existe.")
        // }

        if (comentario.comentarioPadreId != null) {
            val comentarioPadre = dao.findById(comentario.comentarioPadreId)
                ?: throw IllegalArgumentException("El comentario padre con ID ${comentario.comentarioPadreId} no existe.")

        }

        var comentarioConImagen = comentario

        if (newImageBytes != null && newImageOriginalFileName != null) {
            try {
                val relativePath = imagenService.saveImageAndGetRelativePath(
                    newImageBytes,
                    newImageOriginalFileName,
                    newImageMimeType,
                    TipoEntidad.COMENTARIO
                )
                comentarioConImagen = comentario.copy(imagen = relativePath)
            } catch (e: Exception) {
                throw e
            }
        }
        return dao.create(comentarioConImagen)
    }

    fun updateComentario(id: Int, comentario: Comentario, newImageBytes: ByteArray? = null, newImageMimeType: String? = null, newImageOriginalFileName: String? = null): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de comentario inválido para actualización: $id")
        }
        validateComentario(comentario, isUpdate = true)

        val comentarioExistente = dao.findById(id) ?: return false


        var comentarioConImagen = comentario

        if (newImageBytes != null && newImageOriginalFileName != null) {
            if (comentarioExistente.imagen != null) {
                imagenService.deleteImageByRelativePath(comentarioExistente.imagen)
            }
            try {
                // 6.2. Subir la nueva imagen y obtener la ruta relativa
                val relativePath = imagenService.saveImageAndGetRelativePath(
                    newImageBytes,
                    newImageOriginalFileName,
                    newImageMimeType,
                    TipoEntidad.COMENTARIO
                )
                comentarioConImagen = comentario.copy(imagen = relativePath)
            } catch (e: Exception) {
                comentarioConImagen = comentario.copy(imagen = comentarioExistente.imagen)
            }
        } else if (newImageBytes == null && comentario.imagen == null) {
            if (comentarioExistente.imagen != null) {
                imagenService.deleteImageByRelativePath(comentarioExistente.imagen)
            }
            comentarioConImagen = comentario.copy(imagen = null)
        }

        return dao.update(id, comentarioConImagen)
    }

    fun deleteComentario(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de comentario inválido para eliminación: $id")
        }

        val comentarioExistente = dao.findById(id) ?: return false

        if (comentarioExistente.imagen != null) {
            imagenService.deleteImageByRelativePath(comentarioExistente.imagen)
        }

        return dao.delete(id)
    }

    private fun validateComentario(comentario: Comentario, isUpdate: Boolean) {
        if (!isUpdate && comentario.contenido.isBlank()) {
            throw IllegalArgumentException("El contenido del comentario es obligatorio.")
        }
        if (comentario.autorId <= 0) {
            throw IllegalArgumentException("El ID del autor debe ser un número positivo.")
        }
        if (comentario.postId <= 0) {
            throw IllegalArgumentException("El ID del post debe ser un número positivo.")
        }
        if (comentario.comentarioPadreId != null && comentario.comentarioPadreId <= 0) {
            throw IllegalArgumentException("El ID del comentario padre, si se proporciona, debe ser un número positivo.")
        }
    }
}