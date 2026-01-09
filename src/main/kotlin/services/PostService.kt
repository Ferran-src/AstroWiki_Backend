package org.example.services

import org.example.daos.PostDAO
import org.example.models.Post

class PostService(
    private val dao: PostDAO = PostDAO()
) {
    fun getAll(): List<Post> = dao.getAll()

    fun getById(id: Int): Post? = dao.getById(id)

    fun getByAutorId(autorId: Int): List<Post> = dao.getByAutorId(autorId)

    fun getBySeccionId(seccionId: Int): List<Post> = dao.getBySeccionId(seccionId)

    fun searchByTitulo(query: String): List<Post> = dao.searchByTitulo(query)

    fun create(post: Post): Int = dao.create(post)

    fun update(id: Int, post: Post): Boolean = dao.update(id, post)

    fun updateLike(id: Int, like: Boolean): Boolean = dao.updateLike(id, like)

    fun delete(id: Int): Boolean = dao.delete(id)

    fun getRecentPosts(limit: Int = 10, offset: Int = 0): List<Post> =
        dao.getRecentPosts(limit, offset)

    fun getPostsWithLikes(): List<Post> = dao.getPostsWithLikes()

    // Método para contar posts por autor
    fun countByAutorId(autorId: Int): Int = dao.countByAutorId(autorId)

    // Método para contar posts por sección
    fun countBySeccionId(seccionId: Int): Int = dao.countBySeccionId(seccionId)
}