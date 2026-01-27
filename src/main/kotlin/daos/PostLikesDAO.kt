package org.example.daos


import org.jetbrains.exposed.sql.selectAll
import org.example.database.PostLikes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PostLikesDAO {

    fun exists(postId: Int, usuarioId: Int): Boolean = transaction {
        PostLikes.selectAll().where {
            (PostLikes.postId eq postId) and
                    (PostLikes.usuarioId eq usuarioId)
        }.count() > 0
    }

    fun like(postId: Int, usuarioId: Int): Boolean = transaction {
        PostLikes.insertIgnore {
            it[PostLikes.postId] = postId
            it[PostLikes.usuarioId] = usuarioId
        }.insertedCount > 0
    }

    fun unlike(postId: Int, usuarioId: Int): Boolean = transaction {
        PostLikes.deleteWhere {
            (PostLikes.postId eq postId) and
                    (PostLikes.usuarioId eq usuarioId)
        } > 0
    }

    fun findPostsLikedByUsuario(usuarioId: Int): List<org.example.models.PostLikes> = transaction {
        PostLikes
            .select(PostLikes.postId)
            .where { PostLikes.usuarioId eq usuarioId }
            .map { org.example.models.PostLikes(it[PostLikes.postId].value, usuarioId)}
    }

    fun findUsuariosWhoLikedPost(postId: Int): List<org.example.models.PostLikes> = transaction {
        PostLikes
            .select(PostLikes.usuarioId)
            .where { PostLikes.postId eq postId }
            .map { org.example.models.PostLikes(postId, it[PostLikes.usuarioId].value)}
    }
}
