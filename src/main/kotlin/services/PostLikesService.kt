package org.example.services

import org.example.daos.PostLikesDao
import org.example.database.Posts
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PostLikesService {

    /**
     * Toggle like:
     * true  -> dio like
     * false -> quitó like
     */
    fun toggleLike(postId: Int, usuarioId: Int): Boolean {
        return if (PostLikesDao.exists(postId, usuarioId)) {
            PostLikesDao.unlike(postId, usuarioId)
            false
        } else {
            PostLikesDao.like(postId, usuarioId)
            true
        }
    }

    fun getPostsLikedByUsuario(usuarioId: Int): List<Int> =
        PostLikesDao.findPostsLikedByUsuario(usuarioId)

    fun getUsuariosWhoLikedPost(postId: Int): List<Int> =
        PostLikesDao.findUsuariosWhoLikedPost(postId)
}