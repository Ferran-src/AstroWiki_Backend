package org.example.services


import org.example.daos.SeccionDAO
import org.example.daos.SeguimientoSeccionesDAO
import org.example.models.SeguimientoSeccion
import org.example.daos.UsuarioDAO
import org.example.database.Secciones
import org.example.database.Usuarios
import org.jetbrains.exposed.dao.id.EntityID

class SeguimientoSeccionesService {
    private val dao = SeguimientoSeccionesDAO
    private val usuarioDao = UsuarioDAO
    private val seccionDao = SeccionDAO

    fun getAllSeguimientos(): List<SeguimientoSeccion> {
        return dao.findAll()
    }

    fun getSeguimientosByUsuarioId(usuarioId: Int): List<SeguimientoSeccion> {

        if (usuarioId <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }

        return dao.findByUsuarioId(usuarioId)
    }

    fun getSeguimientosBySeccionId(seccionId: Int): List<SeguimientoSeccion> {
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }

        return dao.findBySeccionId(seccionId)
    }

    fun getSeguimientoByUsuarioAndSeccion(usuarioId: Int, seccionId: Int): SeguimientoSeccion? {
        if (usuarioId <= 0 || seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido: usuarioId=$usuarioId, seccionId=$seccionId")
        }
        return dao.findByUsuarioAndSeccion(usuarioId, seccionId)
    }

    fun createSeguimiento(usuarioId: EntityID<Int>, seccionId: EntityID<Int>): SeguimientoSeccion {

        validateIds(usuarioId.value, seccionId.value)

        usuarioDao.findById(usuarioId.value) ?: throw IllegalArgumentException("El usuario con ID $usuarioId no existe.")

        seccionDao.findByIdWithCreator(seccionId.value)
            ?: throw IllegalArgumentException("La sección con ID $seccionId no existe.")

        if (dao.findByUsuarioAndSeccion(usuarioId.value, seccionId.value) != null) {
            throw IllegalArgumentException("El usuario con ID $usuarioId ya está siguiendo la sección con ID $seccionId.")
        }

        return dao.create(usuarioId, seccionId)
    }

    fun createSeguimientoConObjeto(seguimiento: SeguimientoSeccion): SeguimientoSeccion {

        validateIds(seguimiento.usuarioId, seguimiento.seccionId)

        usuarioDao.findById(seguimiento.usuarioId)
            ?: throw IllegalArgumentException("El usuario con ID ${seguimiento.usuarioId} no existe.")

        seccionDao.findByIdWithCreator(seguimiento.seccionId)
            ?: throw IllegalArgumentException("La sección con ID ${seguimiento.seccionId} no existe.")

        if (dao.findByUsuarioAndSeccion(seguimiento.usuarioId, seguimiento.seccionId) != null) {
            throw IllegalArgumentException("El usuario con ID ${seguimiento.usuarioId} ya está siguiendo la sección con ID ${seguimiento.seccionId}.")
        }

        return dao.create(EntityID(seguimiento.usuarioId, Usuarios), EntityID(seguimiento.seccionId, Secciones))
    }


    fun deleteSeguimiento(usuarioId: Int, seccionId: Int): Boolean {

        if (usuarioId <= 0 || seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido para eliminación: usuarioId=$usuarioId, seccionId=$seccionId")
        }

        dao.findByUsuarioAndSeccion(usuarioId, seccionId) ?: return false
        return dao.delete(usuarioId, seccionId)
    }

    fun deleteSeguimientoConObjeto(seguimiento: SeguimientoSeccion): Boolean {

        if (seguimiento.usuarioId <= 0 || seguimiento.seccionId <= 0) {
            throw IllegalArgumentException("ID de usuario o sección inválido para eliminación: ${seguimiento.usuarioId}, ${seguimiento.seccionId}")
        }


        val seguimientoExistente = dao.findByUsuarioAndSeccion(seguimiento.usuarioId, seguimiento.seccionId) ?: return false

        return dao.delete(seguimiento.usuarioId, seguimiento.seccionId)
    }

    private fun validateIds(usuarioId: Int, seccionId: Int) {
        if (usuarioId <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }
    }
}