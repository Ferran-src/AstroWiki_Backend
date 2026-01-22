package org.example.daos
import org.example.models.Comentario
import org.example.database.Comentarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
object ComentarioDAO {
    fun findAll(): List<Comentario> = transaction {
        Comentarios.selectAll().orderBy(Comentarios.fechaCreacion to SortOrder.ASC).map { row ->
            rowToComentario(row)
        }
    }

    fun findByPostId(postId: Int): List<Comentario> = transaction {
        Comentarios.selectAll().where { Comentarios.postId eq postId }.orderBy(Comentarios.fechaCreacion to SortOrder.ASC).map { row ->
            rowToComentario(row)
        }
    }

    fun findByAutorId(autorId: Int): List<Comentario> = transaction {
        Comentarios.selectAll().where { Comentarios.autorId eq autorId }.orderBy(Comentarios.fechaCreacion to SortOrder.ASC).map { row ->
            rowToComentario(row)
        }
    }

    fun findById(id: Int): Comentario? = transaction {
        Comentarios.selectAll().where { Comentarios.id eq id }.firstOrNull()?.let { row ->
            rowToComentario(row)
        }
    }

    fun create(comentario: Comentario): Comentario = transaction {
        val insertedId = Comentarios.insert {
            it[contenido] = comentario.contenido
            it[imagen] = comentario.imagen ?: ""
            it[autorId] = comentario.autorId
            it[postId] = comentario.postId
            comentario.comentarioPadreId?.let { parentId -> it[comentarioPadreId] = parentId }

        } get Comentarios.id
        comentario.copy(idComentario = insertedId.value, likeCount = "0")
    }

    fun update(id: Int, comentario: Comentario): Boolean = transaction {

        val updatedRows = Comentarios.update({ Comentarios.id eq id }) {
            it[contenido] = comentario.contenido
            it[imagen] = comentario.imagen ?: ""
            it[autorId] = comentario.autorId
            it[postId] = comentario.postId

        }
        updatedRows > 0
    }

    fun delete(id: Int): Boolean = transaction {
        // DELETE FROM comentarios WHERE id_comentario = ?;
        val deletedRows = Comentarios.deleteWhere { Comentarios.id eq id }
        deletedRows > 0
    }

    private fun rowToComentario(row: ResultRow): Comentario {
        return Comentario(
            idComentario = row[Comentarios.id].value,
            contenido = row[Comentarios.contenido],
            imagen = row[Comentarios.imagen].takeIf { !it.isNullOrEmpty() },
            likeCount = row[Comentarios.contadorLikes].toString(),
            autorId = row[Comentarios.autorId].value,
            postId = row[Comentarios.postId].value,
            comentarioPadreId = row[Comentarios.comentarioPadreId],
            fechaCreacion = row[Comentarios.fechaCreacion].toString()
        )
    }
}