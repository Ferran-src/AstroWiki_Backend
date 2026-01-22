package org.example.daos

import org.jetbrains.exposed.sql.selectAll
import org.example.database.Posts
import org.example.models.Post
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PostDAO {

    fun getAll(): List<Post> = transaction {
        Posts.selectAll().map { rowToPost(it) }
    }

    fun getById(id: Int): Post? = transaction {
        Posts
            .selectAll().where { Posts.id eq id }
            .map { rowToPost(it) }
            .singleOrNull()
    }

    fun getByAutorId(autorId: Int): List<Post> = transaction {
        Posts
            .selectAll().where { Posts.autorId eq autorId }
            .orderBy(Posts.fechaCreacion to SortOrder.DESC)
            .map { rowToPost(it) }
    }

    fun getBySeccionId(seccionId: Int): List<Post> = transaction {
        Posts
            .selectAll().where { Posts.seccionId eq seccionId }
            .orderBy(Posts.fechaCreacion to SortOrder.DESC)
            .map { rowToPost(it) }
    }

    fun getByTitulo(query: String): List<Post> = transaction {
        Posts
            .selectAll().where { Posts.titulo like "%$query%" }
            .orderBy(Posts.fechaCreacion to SortOrder.DESC)
            .map { rowToPost(it) }
    }

    fun create(post: Post): Int = transaction {
        Posts.insert {
            it[titulo] = post.titulo
            it[contenido] = post.contenido
            it[imagen] = post.imagen
            it[autorId] = post.autorId
            it[seccionId] = post.seccionId
        }[Posts.id].value
    }

    fun update(id: Int, post: Post): Boolean = transaction {
        val updateCount = Posts.update({ Posts.id eq id }) { row ->
            row[titulo] = post.titulo
            row[contenido] = post.contenido
            row[imagen] = post.imagen
            row[autorId] = post.autorId
            row[seccionId] = post.seccionId
        }
        updateCount > 0
    }

    fun delete(id: Int): Boolean = transaction {
        val deleteCount = Posts.deleteWhere { Posts.id eq id }
        deleteCount > 0
    }

    fun getRecentPosts(limit: Int = 10, offset: Int = 0): List<Post> = transaction {
        Posts
            .selectAll()
            .orderBy(Posts.fechaCreacion to SortOrder.DESC)
            .limit(limit).offset(offset.toLong())
            .map { rowToPost(it) }
    }

    fun countByAutorId(autorId: Int): Int = transaction {
        Posts
            .selectAll().where { Posts.autorId eq autorId }
            .count()
            .toInt()
    }

    fun countBySeccionId(seccionId: Int): Int = transaction {
        Posts
            .selectAll().where { Posts.seccionId eq seccionId }
            .count()
            .toInt()
    }

    // Función privada para convertir ResultRow a Post
    private fun rowToPost(row: ResultRow): Post = Post(
        idPost = row[Posts.id].value,
        titulo = row[Posts.titulo],
        contenido = row[Posts.contenido],
        imagen = row[Posts.imagen].takeIf { it != null && !it.isEmpty() },
        likeCount = row[Posts.contadorLikes],
        comentarioCount = row[Posts.contadorComentarios],
        autorId = row[Posts.autorId].value,
        seccionId = row[Posts.seccionId].value,
        fechaCreacion = row[Posts.fechaCreacion],
    )
}