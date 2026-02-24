package org.example.services

import org.example.daos.SeccionDAO
import org.example.models.Seccion
import org.example.daos.UsuarioDAO

class SeccionesService {
    private val dao = SeccionDAO
    private val usuarioDao = UsuarioDAO
    private val imagenService= ImagenService(UPLOAD_DIR_PATH)

    fun getAllSeccionesWithCreator(): List<Seccion> {

        return dao.findAllWithCreator()
    }

    fun getSeccionByIdWithCreator(id: Int): Seccion? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $id")
        }
        return dao.findByIdWithCreator(id)
    }

    fun createSeccion(
        seccion: Seccion,
        newImageBytes: ByteArray? = null,
        newImageMimeType: String? = null,
        newImageOriginalFileName: String? = null
    ): Seccion {
        validateSeccion(seccion, isUpdate = false)


        seccion.creadorId?.let { id ->
            val creador = usuarioDao.findById(id)
                ?: throw IllegalArgumentException("El creador con ID $id no existe.")
        }

        var seccionWithImage = seccion

        // 3. Manejar imagen si se proporciona
        if (newImageBytes != null && newImageOriginalFileName != null) {
            try {

                val relativePath = imagenService.saveImageAndGetRelativePath(
                    newImageBytes,
                    newImageOriginalFileName,
                    newImageMimeType,
                    TipoEntidad.SECCION
                )

                seccionWithImage = seccion.copy(imagen = relativePath)
            } catch (e: Exception) {

                throw e
            }
        }
        return dao.create(seccionWithImage)
    }


// services/SeccionService.kt (fragmento para updateSeccion)
// Asumiendo que tienes SeccionDAO, UsuarioDAO, ImagenService inyectados

    fun updateSeccion(
        id: Int,
        updates: Seccion,
        newImageBytes: ByteArray? = null,
        newImageMimeType: String? = null,
        newImageOriginalFileName: String? = null
    ): Boolean {
        val existingSeccion = dao.findByIdWithCreator(id)
            ?: return false

        updates.creadorId?.let { id ->
            usuarioDao.findById(id)
                ?: throw IllegalArgumentException("El nuevo creador con ID $id no existe.")
        }

        val updatedTitulo = updates.titulo.takeIf { !it.isNullOrEmpty() } ?: existingSeccion.titulo
        val updatedDescripcion = updates.descripcion
        val updatedCreadorId = updates.creadorId ?: existingSeccion.creadorId


        var updatedImagenPath = existingSeccion.imagen

        if (newImageBytes != null && newImageOriginalFileName != null) {

            try {

                val newRelativePath = imagenService.saveImageAndGetRelativePath(
                    newImageBytes,
                    newImageOriginalFileName,
                    newImageMimeType,
                    TipoEntidad.SECCION
                )
                updatedImagenPath = newRelativePath


            } catch (e: Exception) {
                throw e
            }
        } else if (updates.imagen == "") {

            updatedImagenPath = null
        }


        val seccionToUpdate = existingSeccion.copy(
            titulo = updatedTitulo,
            descripcion = updatedDescripcion,
            creadorId = updatedCreadorId,
            imagen = updatedImagenPath
        )

        validateSeccion(seccionToUpdate, isUpdate = true)

        return dao.update(id, seccionToUpdate)
    }

    fun deleteSeccion(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para eliminación: $id")
        }

        return dao.delete(id)
    }

    private fun validateSeccion(seccion: Seccion, isUpdate: Boolean) {
        if (!isUpdate && seccion.titulo.isBlank()) {
            throw IllegalArgumentException("El título de la sección es obligatorio.")
        }
        if (seccion.creadorId!! <= 0) {
            throw IllegalArgumentException("El ID del creador debe ser un número positivo.")
        }
    }
}