package org.example.services


import org.example.daos.SeguimientoSeccionesDAO // Importa tu DAO
import org.example.models.SeguimientoSeccion // Importa tu modelo

import org.example.daos.UsuarioDAO

class SeguimientoSeccionesService {
    private val dao = SeguimientoSeccionesDAO
    private val usuarioDao = UsuarioDAO // Instancia del UsuarioDao para validaciones
    private val seccionDao

    fun getAllSeguimientos(): List<SeguimientoSeccion> {
        // Aquí podrías aplicar lógica adicional si es necesario antes de delegar al DAO
        // Por ejemplo, filtrar si la lógica de negocio lo requiere
        // o verificar permisos del usuario autenticado.
        // Para este ejemplo, simplemente delegamos.
        return dao.findAll()
    }

    fun getSeguimientosByUsuarioId(usuarioId: Int): List<SeguimientoSeccion> {
        // Lógica de negocio: Validar ID del usuario
        if (usuarioId <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }
        // Opcional: Verificar si el usuario existe antes de buscar sus seguimientos
        // if (usuarioDao.findById(usuarioId) == null) {
        //     throw EntityNotFoundException("Usuario con ID $usuarioId no encontrado.")
        // }
        // Delegar al DAO
        return dao.findByUsuarioId(usuarioId)
    }

    fun getSeguimientosBySeccionId(seccionId: Int): List<SeguimientoSeccion> {
        // Lógica de negocio: Validar ID de la sección
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }
        // Opcional: Verificar si la sección existe antes de buscar sus seguidores
        // if (seccionDao.findByIdWithCreator(seccionId) == null) {
        //     throw EntityNotFoundException("Sección con ID $seccionId no encontrada.")
        // }
        // Delegar al DAO
        return dao.findBySeccionId(seccionId)
    }

    fun getSeguimientoByUsuarioAndSeccion(usuarioId: Int, seccionId: Int): SeguimientoSeccion? {
        // Lógica de negocio: Validar IDs
        if (usuarioId <= 0 || seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido: usuarioId=$usuarioId, seccionId=$seccionId")
        }
        // Delegar al DAO
        return dao.findByUsuarioAndSeccion(usuarioId, seccionId)
    }

    fun createSeguimiento(usuarioId: Int, seccionId: Int): SeguimientoSeccion {

        validateIds(usuarioId, seccionId)

        // 2. Verificar si el usuario existe
        val usuario = usuarioDao.findById(usuarioId)
        if (usuario == null) {
            throw IllegalArgumentException("El usuario con ID $usuarioId no existe.")
        }

        // 3. Verificar si la sección existe
        val seccion = seccionDao.findByIdWithCreator(seccionId)
        if (seccion == null) {
            throw IllegalArgumentException("La sección con ID $seccionId no existe.")
        }

        // 4. Verificar si ya existe el seguimiento (evitar duplicados)
        if (dao.findByUsuarioAndSeccion(usuarioId, seccionId) != null) {
            throw IllegalArgumentException("El usuario con ID $usuarioId ya está siguiendo la sección con ID $seccionId.")
        }

        // 5. Delegar al DAO para crear la relación
        return dao.create(usuarioId, seccionId)
    }

    fun createSeguimientoConObjeto(seguimiento: SeguimientoSeccion): SeguimientoSeccion {
        // Lógica de negocio para creación usando un objeto:
        // 1. Validar IDs desde el objeto
        validateIds(seguimiento.usuarioId, seguimiento.seccionId)

        // 2. Verificar si el usuario existe
        val usuario = usuarioDao.findById(seguimiento.usuarioId)
        if (usuario == null) {
            throw IllegalArgumentException("El usuario con ID ${seguimiento.usuarioId} no existe.")
        }

        // 3. Verificar si la sección existe
        val seccion = seccionDao.findByIdWithCreator(seguimiento.seccionId)
        if (seccion == null) {
            throw IllegalArgumentException("La sección con ID ${seguimiento.seccionId} no existe.")
        }

        // 4. Verificar si ya existe el seguimiento (evitar duplicados)
        if (dao.findByUsuarioAndSeccion(seguimiento.usuarioId, seguimiento.seccionId) != null) {
            throw IllegalArgumentException("El usuario con ID ${seguimiento.usuarioId} ya está siguiendo la sección con ID ${seguimiento.seccionId}.")
        }

        // 5. Delegar al DAO para crear la relación
        return dao.create(seguimiento.usuarioId, seguimiento.seccionId) // Puedes adaptar el DAO para recibir el objeto si lo prefieres
    }


    fun deleteSeguimiento(usuarioId: Int, seccionId: Int): Boolean {
        // Lógica de negocio para eliminación:
        // 1. Validar IDs
        if (usuarioId <= 0 || seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido para eliminación: usuarioId=$usuarioId, seccionId=$seccionId")
        }

        // 2. Verificar si la relación existe antes de intentar eliminarla
        val seguimientoExistente = dao.findByUsuarioAndSeccion(usuarioId, seccionId)
        if (seguimientoExistente == null) {
            // Opcional: Lanzar una excepción específica
            // throw SeguimientoNotFoundException("No se encontró el seguimiento del usuario $usuarioId a la sección $seccionId.")
            return false // Indica que no se pudo eliminar porque no existía
        }

        // 3. Delegar al DAO para eliminar la relación
        return dao.delete(usuarioId, seccionId)
    }

    fun deleteSeguimientoConObjeto(seguimiento: SeguimientoSeccion): Boolean {
        // Lógica de negocio para eliminación usando un objeto:
        // 1. Validar IDs desde el objeto
        if (seguimiento.usuarioId <= 0 || seguimiento.seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido para eliminación: ${seguimiento.usuarioId}, ${seguimiento.seccionId}")
        }

        // 2. Verificar si la relación existe antes de intentar eliminarla
        val seguimientoExistente = dao.findByUsuarioAndSeccion(seguimiento.usuarioId, seguimiento.seccionId)
        if (seguimientoExistente == null) {
            // Opcional: Lanzar una excepción específica
            // throw SeguimientoNotFoundException("No se encontró el seguimiento del usuario ${seguimiento.usuarioId} a la sección ${seguimiento.seccionId}.")
            return false // Indica que no se pudo eliminar porque no existía
        }

        // 3. Delegar al DAO para eliminar la relación
        return dao.delete(seguimiento.usuarioId, seguimiento.seccionId)
    }

    // Función privada para validar IDs comunes
    private fun validateIds(usuarioId: Int, seccionId: Int) {
        if (usuarioId <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }
    }
}