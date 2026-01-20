package org.example.daos


import org.jetbrains.exposed.sql.selectAll
import org.example.database.PostLikes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PostLikesDao {

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

    fun findPostsLikedByUsuario(usuarioId: Int): List<Int> = transaction {
        PostLikes
            .select(PostLikes.postId)
            .where { PostLikes.usuarioId eq usuarioId }
            .map { it[PostLikes.postId] }
    }

    fun findUsuariosWhoLikedPost(postId: Int): List<Int> = transaction {
        PostLikes
            .select(PostLikes.usuarioId)
            .where { PostLikes.postId eq postId }
            .map { it[PostLikes.usuarioId] }
    }
}
