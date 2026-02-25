package org.example.services

import org.example.daos.ArticuloDAO
import org.example.models.Articulo

class ArticuloService {

    private val dao: ArticuloDAO = ArticuloDAO
    private val articuloCategoriaService: ArticuloCategoriaService = ArticuloCategoriaService()
    //private val dao = ArticuloDAO


    fun getAll(): List<Articulo> {
        return dao.getAll()
    }

    fun getById(id: Int): Articulo? {
        if (id <= 0) {
            throw IllegalArgumentException("ID de artículo inválido: $id")
        }
        return dao.getById(id)
    }

    fun create(articulo: Articulo): Int {
        validateArticulo(articulo, isUpdate = false)

        return dao.create(articulo)
    }

    fun update(id: Int, articulo: Articulo): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de artículo inválido para actualización: $id")
        }

        validateArticulo(articulo, isUpdate = true)

        val articuloExistente = dao.getById(id) ?: return false

        return dao.update(id, articulo)
    }

    fun delete(id: Int): Boolean {
        if (id <= 0) {
            throw IllegalArgumentException("ID de artículo inválido para eliminación: $id")
        }

        val articuloExistente = dao.getById(id) ?: return false

        return dao.delete(id)
    }

    private fun validateArticulo(articulo: Articulo, isUpdate: Boolean) {
        if (!isUpdate && articulo.titulo.isBlank()) {
            throw IllegalArgumentException("El título del artículo es obligatorio.")
        }

        if (articulo.contenido.isBlank()) {
            throw IllegalArgumentException("El contenido del artículo es obligatorio.")
        }
    }
    fun getArticulosByCategoriaId(categoriaId: Int): List<Articulo> {
        // 1. Obtener los IDs de artículos asociados a la categoría usando el servicio de la relación
        val articuloIds = articuloCategoriaService.getArticulosIdsForCategoria(categoriaId)

        if (articuloIds.isEmpty()) {
            return emptyList() // Si no hay artículos en la categoría, devuelve lista vacía
        }

        // 2. Obtener los artículos completos usando los IDs obtenidos
        //    La implementación exacta dependerá de lo que ofrezca tu ArticuloDAO.
        //    Puede que necesites un método como findMultipleByIds o adaptar getAll() con un filtro.
        //    Ejemplo (asumiendo que tu DAO tiene un método adecuado o puedes extenderlo):
        return dao.getByIds(articuloIds.map { it.value }) // Convertimos EntityID<Int> a Int
    }
}