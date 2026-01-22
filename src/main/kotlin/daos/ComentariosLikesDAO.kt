package org.example.daos
import org.example.models.ComentarioLike
import org.example.database.ComentariosLikes
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
object ComentariosLikesDAO {
    fun findAll(): List<ComentarioLike> = transaction {
        ComentariosLikes.selectAll().map { row ->
            rowToComentarioLike(row)
        }
    }


    fun findByComentarioId(comentarioId: EntityID<Int>): List<ComentarioLike> = transaction {
        ComentariosLikes.selectAll().where { ComentariosLikes.comentarioId eq comentarioId }.map { row ->
            rowToComentarioLike(row)
        }
    }

    fun findByUsuarioId(usuarioId: EntityID<Int>): List<ComentarioLike> = transaction {
        ComentariosLikes.selectAll().where { ComentariosLikes.usuarioId eq usuarioId }.map { row ->
            rowToComentarioLike(row)
        }
    }

    fun findByUsuarioAndComentario(usuarioId: Int, comentarioId: Int): ComentarioLike? = transaction {
        ComentariosLikes.selectAll()
            .where { ComentariosLikes.usuarioId eq usuarioId and (ComentariosLikes.comentarioId eq comentarioId) }.firstOrNull()?.let { row ->
            rowToComentarioLike(row)
        }
    }


    fun create(usuarioId: EntityID<Int>, comentarioId: EntityID<Int>): ComentarioLike = transaction {
   ComentariosLikes.insert {
            it[ComentariosLikes.usuarioId] = usuarioId
            it[ComentariosLikes.comentarioId] = comentarioId
        }

        ComentarioLike(comentarioid = comentarioId, usuarioid = usuarioId)
    }


    fun delete(usuarioId: EntityID<Int>, comentarioId: EntityID<Int>): Boolean = transaction {

        val deletedRows = ComentariosLikes.deleteWhere {
            ComentariosLikes.usuarioId eq usuarioId and (ComentariosLikes.comentarioId eq comentarioId)
        }
        deletedRows > 0
    }

    private fun rowToComentarioLike(row: ResultRow): ComentarioLike {
        return ComentarioLike(
            comentarioid = row[ComentariosLikes.comentarioId],
            usuarioid = row[ComentariosLikes.usuarioId]
        )
    }
}