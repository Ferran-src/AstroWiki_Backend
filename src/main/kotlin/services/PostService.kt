package org.example.services

import org.example.daos.PostDAO
import org.example.daos.UsuarioDAO
import org.example.models.Post

class PostService {
    private val dao: PostDAO = PostDAO()
    private val usuarioDao: UsuarioDAO = UsuarioDAO
    private val imagenService = ImagenService(UPLOAD_DIR_PATH)

    fun getAll(): List<Post> {
        return dao.getAll()
    }

    fun getById(id: Int): Post? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de post inválido: $id")
        }
        return dao.getById(id)
    }

    fun getByAutorId(autorId: Int): List<Post> {
        if (autorId <= 0) {
            throw IllegalArgumentException("ID de autor inválido: $autorId")
        }
        return dao.getByAutorId(autorId)
    }

    fun getBySeccionId(seccionId: Int): List<Post> {
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }
        return dao.getBySeccionId(seccionId)
    }

    fun searchByTitulo(query: String): List<Post> {
        if (query.isBlank()) {
            throw IllegalArgumentException("El texto de búsqueda no puede estar vacío.")
        }
        return dao.getByTitulo(query)
    }

    fun create(post: Post): Int {
        validatePost(post, isUpdate = false)

        val autor = usuarioDao.findById(post.autorId)
        if (autor == null) {
            throw IllegalArgumentException("El autor con ID ${post.autorId} no existe.")
        }

        return dao.create(post)
    }

    fun update(id: Int, post: Post): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de post inválido para actualización: $id")
        }

        validatePost(post, isUpdate = true)

        val existente = dao.getById(id) ?: return false

        return dao.update(id, post)
    }

    fun delete(id: Int): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de post inválido para eliminación: $id")
        }

        val existingPost = dao.getById(id) ?: return false

        if (existingPost.imagen != null) {
            imagenService.deleteImageByRelativePath(existingPost.imagen)
        }

        return dao.delete(id)
    }

    fun getRecentPosts(limit: Int = 10, offset: Int = 0): List<Post> {
        if (limit <= 0) {
            throw IllegalArgumentException("El límite debe ser mayor que 0.")
        }
        if (offset < 0) {
            throw IllegalArgumentException("El offset no puede ser negativo.")
        }
        return dao.getRecentPosts(limit, offset)
    }

    fun countByAutorId(autorId: Int): Int {
        if (autorId <= 0) {
            throw IllegalArgumentException("ID de autor inválido: $autorId")
        }
        return dao.countByAutorId(autorId)
    }

    fun countBySeccionId(seccionId: Int): Int {
        if (seccionId <= 0) {
            throw IllegalArgumentException("ID de sección inválido: $seccionId")
        }
        return dao.countBySeccionId(seccionId)
    }

    // Utility functions
    private fun validatePost(post: Post, isUpdate: Boolean) {
        if (!isUpdate && post.titulo.isBlank()) {
            throw IllegalArgumentException("El título del post es obligatorio.")
        }

        if (!isUpdate && post.contenido.isBlank()) {
            throw IllegalArgumentException("El contenido del post es obligatorio.")
        }

        if (post.autorId <= 0) {
            throw IllegalArgumentException("El ID del autor debe ser un número positivo.")
        }

        if (post.seccionId <= 0) {
            throw IllegalArgumentException("El ID de la sección debe ser un número positivo.")
        }
    }
}
