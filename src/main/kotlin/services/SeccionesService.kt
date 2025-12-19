package org.example.services

import org.example.models.Seccion
import org.example.daos.SeccionDAO // Importa tu DAO
import org.example.daos.UsuarioDAO // Importa UsuarioDao para validar el creador

class SeccionesService {
    private val dao = SeccionDAO
    private val usuarioDao = UsuarioDAO

    fun getAllSeccionesWithCreator(): List<Seccion> {
        // Aquí podrías aplicar lógica adicional si es necesario antes de delegar al DAO
        // Por ejemplo, filtrar secciones inactivas si la lógica de negocio lo requiere
        // o verificar permisos del usuario autenticado.
        // Para este ejemplo, simplemente delegamos.
        return dao.findAllWithCreator()
    }

    fun getSeccionByIdWithCreator(id: Int): Seccion? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $id")
        }
        return dao.findByIdWithCreator(id)
    }

    fun createSeccion(seccion: Seccion): Seccion {
        // Lógica de negocio para creación:
        // 1. Validar datos de entrada
        validateSeccion(seccion, isUpdate = false)

        // 2. Verificar si el creador (usuario) existe
        val creador = usuarioDao.findById(seccion.creadorId)
        if (creador == null) {
            throw IllegalArgumentException("El usuario creador con ID ${seccion.creadorId} no existe.")
        }

        // 3. Delegar al DAO para guardar
        return dao.create(seccion)
    }

    fun updateSeccion(id: Int, seccion: Seccion): Boolean {
        // Lógica de negocio para actualización:
        // 1. Validar ID
        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para actualización: $id")
        }
        // 2. Validar datos de entrada
        validateSeccion(seccion, isUpdate = true)

        // 3. Verificar si la sección existe
        val seccionExistente = dao.findByIdWithCreator(id) ?:
        return false

        // TODO solo usuario autenticado con jwt y ser dueño de seccion puede modificar

        return dao.update(id, seccion)
    }

    fun deleteSeccion(id: Int): Boolean {

        if (id <= 0) {
            throw IllegalArgumentException("ID de sección inválido para eliminación: $id")
        }
        // 2. Opcional: Verificar dependencias (¿tiene posts, usuarios suscritos, etc.?)
        // Si tiene dependencias, decidir si se permite borrar o se marca como inactiva.
        return dao.delete(id)
    }

    private fun validateSeccion(seccion: Seccion, isUpdate: Boolean) {
        if (!isUpdate && seccion.titulo.isBlank()) { // titulo es obligatorio al crear
            throw IllegalArgumentException("El título de la sección es obligatorio.")
        }
        if (seccion.creadorId <= 0) {
            throw IllegalArgumentException("El ID del creador debe ser un número positivo.")
        }
    }
}