package org.example.daos
import org.example.models.Comentario
import org.example.database.Comentarios // Tu objeto Table de Exposed para comentarios
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

    // Obtener comentarios de un post específico
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

    // Obtener un comentario específico por ID
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

    // Eliminar un comentario
    fun delete(id: Int): Boolean = transaction {
        // DELETE FROM comentarios WHERE id_comentario = ?;
        val deletedRows = Comentarios.deleteWhere { Comentarios.id eq id }
        deletedRows > 0
    }

    // Función privada para mapear una fila de la base de datos a la data class Comentario
    private fun rowToComentario(row: ResultRow): Comentario {
        return Comentario(
            idComentario = row[Comentarios.id].value,
            contenido = row[Comentarios.contenido],
            imagen = row[Comentarios.imagen].takeIf { it != null && !it.isEmpty() }, // Devuelve null si está vacío
            likeCount = row[Comentarios.likeCount].toString(), // Convierte el INT de la BD a String para el DTO
            autorId = row[Comentarios.autorId],
            postId = row[Comentarios.postId],
            comentarioPadreId = row[Comentarios.comentarioPadreId], // Puede ser null
            fechaCreacion = row[Comentarios.fechaCreacion].toString() // o LocalDateTime
        )
    }
}