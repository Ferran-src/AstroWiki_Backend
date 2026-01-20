package org.example.services

import org.example.daos.SeccionDAO
import org.example.models.Seccion
import org.example.daos.UsuarioDAO
import org.example.models.Usuario

class SeccionesService {
    private val dao = SeccionDAO
    private val usuarioDao = UsuarioDAO

    fun getAllSeccionesWithCreator(): List<Seccion> {

        return dao.findAllWithCreator()
    }

    fun getSeccionByIdWithCreator(id: Int): Seccion? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $id")
        }
        return dao.findByIdWithCreator(id)
    }

    fun createSeccion(seccion: Seccion): Seccion {

        validateSeccion(seccion, isUpdate = false)

        val creador = usuarioDao.findById(seccion.creadorId!! )
        if (creador == null) {
            throw IllegalArgumentException("El usuario creador con ID ${seccion.creadorId} no existe.")
        }

        return dao.create(seccion)
    }

    fun updateSeccion(id: Int, seccion: Seccion): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para actualización: $id")
        }
        validateSeccion(seccion, isUpdate = true)

        val seccionExistente = dao.findByIdWithCreator(id) ?: return false

        //TODO solo creador puede actualizar

        return dao.update(id, seccion)
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