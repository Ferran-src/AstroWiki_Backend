package org.example.services

import org.example.daos.SeccionDAO
import org.example.models.Seccion
import org.example.daos.UsuarioDAO
import org.example.models.Usuario

class SeccionesService {
    private val dao = SeccionDAO
    private val usuarioDao = UsuarioDAO

    fun getAllSeccionesWithCreator(): List<Seccion> {

        // verificar permisos del usuario autenticado.
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

        val creador = usuarioDao.findById(seccion.creadorId)
        if (creador == null) {
            throw IllegalArgumentException("El usuario creador con ID ${seccion.creadorId} no existe.")
        }

        return dao.create(seccion)
    }

    fun updateSeccion(id: Int, seccion: Seccion): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para actualización: $id")
        }
        // 2. Validar datos de entrada
        validateSeccion(seccion, isUpdate = true)

        // 3. Verificar si la sección existe
        val seccionExistente = dao.findByIdWithCreator(id) ?: // Opcional: Lanzar una excepción específica

        return false

        //TODO solo creador puede actualizar

        // 5. Delegar al DAO para actualizar
        return dao.update(id, seccion)
    }

    fun deleteSeccion(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para eliminación: $id")
        }
        // Verificar dependencias (¿tiene posts, usuarios suscritos, etc.?)
        // Si tiene dependencias, decidir si se permite borrar o se marca como inactiva.

        return dao.delete(id)
    }

    private fun validateSeccion(seccion: Seccion, isUpdate: Boolean) {
        if (!isUpdate && seccion.titulo.isBlank()) { // titulo es obligatorio al crear
            throw IllegalArgumentException("El título de la sección es obligatorio.")
        }
        // Añadir más validaciones según sea necesario (longitud del título, contenido de la descripción, etc.)
        if (seccion.creadorId <= 0) {
            throw IllegalArgumentException("El ID del creador debe ser un número positivo.")
        }
    }
}