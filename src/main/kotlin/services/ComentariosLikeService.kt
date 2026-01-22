package org.example.services

import io.ktor.server.plugins.NotFoundException
import org.example.daos.ComentariosLikesDAO
import org.example.models.ComentarioLike
import org.example.daos.UsuarioDAO
import org.example.daos.ComentarioDAO
import org.jetbrains.exposed.dao.id.EntityID

class ComentariosLikesService {
    private val dao = ComentariosLikesDAO
    private val usuarioDao = UsuarioDAO
    private val comentariosDao = ComentarioDAO

    fun getAllLikes(): List<ComentarioLike> {

        return dao.findAll()
    }

    fun getLikesByComentarioId(comentarioId: EntityID<Int>): List<ComentarioLike> {
        if (comentarioId.value <= 0) {
            throw IllegalArgumentException("ID de comentario inválido: $comentarioId")
        }
         if (comentariosDao.findById(comentarioId.value) == null) {
             throw NotFoundException("Comentario con ID $comentarioId no encontrado.")
         }

        return dao.findByComentarioId(comentarioId)
    }

    fun getLikesByUsuarioId(usuarioId: EntityID<Int>): List<ComentarioLike> {
        if (usuarioId.value <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }
         if (usuarioDao.findById(usuarioId.value) == null) {
             throw NotFoundException("Usuario con ID $usuarioId no encontrado.")
         }

        return dao.findByUsuarioId(usuarioId)
    }

    fun getLikeByUsuarioAndComentario(usuarioId: Int, comentarioId: Int): ComentarioLike? {

        validateIds(usuarioId, comentarioId)

        return dao.findByUsuarioAndComentario(usuarioId, comentarioId)
    }

    fun addLike(usuarioId: EntityID<Int>, comentarioId: EntityID<Int>): ComentarioLike {
        validateIds(usuarioId.value, comentarioId.value)

        val usuario =
            usuarioDao.findById(usuarioId.value) ?: throw IllegalArgumentException("El usuario con ID $usuarioId no existe.")

        val comentario = comentariosDao.findById(comentarioId.value)
            ?: throw IllegalArgumentException("El comentario con ID $comentarioId no existe.")

        if (dao.findByUsuarioAndComentario(usuarioId.value, comentarioId.value) != null) {
            throw IllegalArgumentException("El usuario con ID $usuarioId ya ha dado like al comentario con ID $comentarioId.")
        }
        return dao.create(usuarioId, comentarioId)
    }

    fun removeLike(usuarioId: EntityID<Int>, comentarioId: EntityID<Int>): Boolean {
        if (usuarioId.value <= 0 || comentarioId.value <= 0) {
            throw IllegalArgumentException("ID de usuario o comentario inválido para eliminar like: usuarioId=$usuarioId, comentarioId=$comentarioId")
        }

        val likeExistente = dao.findByUsuarioAndComentario(usuarioId.value, comentarioId.value) ?: return false

        return dao.delete(usuarioId, comentarioId)
    }

    private fun validateIds(usuarioId: Int, comentarioId: Int) {
        if (usuarioId <= 0) {
            throw IllegalArgumentException("ID de usuario inválido: $usuarioId")
        }
        if (comentarioId <= 0) {
            throw IllegalArgumentException("ID de comentario inválido: $comentarioId")
        }
    }
}