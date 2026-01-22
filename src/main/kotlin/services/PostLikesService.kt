package org.example.services

import org.example.daos.PostLikesDAO
import org.jetbrains.exposed.dao.id.EntityID

class PostLikesService {

    /**
     * Toggle like:
     * true  -> dio like
     * false -> quitó like
     */
    fun toggleLike(postId: Int, usuarioId: Int): Boolean {
        return if (PostLikesDAO.exists(postId, usuarioId)) {
            PostLikesDAO.unlike(postId, usuarioId)
            false
        } else {
            PostLikesDAO.like(postId, usuarioId)
            true
        }
    }

    fun getPostsLikedByUsuario(usuarioId: Int): List<EntityID<Int>> =
        PostLikesDAO.findPostsLikedByUsuario(usuarioId)

    fun getUsuariosWhoLikedPost(postId: Int): List<EntityID<Int>> =
        PostLikesDAO.findUsuariosWhoLikedPost(postId)
}