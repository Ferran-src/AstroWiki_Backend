package org.example.services

import org.example.daos.ArticuloCategoriaDAO
import org.example.models.ArticuloCategoria

class ArticuloCategoriaService(
    private val dao: ArticuloCategoriaDAO = ArticuloCategoriaDAO
) {
    fun getAll(): List<ArticuloCategoria> = dao.getAll()

    fun getByArticuloId(articuloId: Int): List<ArticuloCategoria> =
        dao.getByArticuloId(articuloId)

    fun getByCategoriaId(categoriaId: Int): List<ArticuloCategoria> =
        dao.getByCategoriaId(categoriaId)

    fun exists(articuloId: Int, categoriaId: Int): Boolean =
        dao.exists(articuloId, categoriaId)

    fun create(relacion: ArticuloCategoria): Boolean =
        dao.create(relacion)

    fun createBatch(relaciones: List<ArticuloCategoria>): Int =
        dao.createBatch(relaciones)

    fun delete(articuloId: Int, categoriaId: Int): Boolean =
        dao.delete(articuloId, categoriaId)

    fun deleteByArticuloId(articuloId: Int): Int =
        dao.deleteByArticuloId(articuloId)

    fun deleteByCategoriaId(categoriaId: Int): Int =
        dao.deleteByCategoriaId(categoriaId)

    fun replaceCategoriasForArticulo(articuloId: Int, categoriasIds: List<Int>): Pair<Int, Int> =
        dao.replaceCategoriasForArticulo(articuloId, categoriasIds)

    // Utility function: obtener IDs de categorías para un artículo
    fun getCategoriasIdsForArticulo(articuloId: Int): List<Int> =
        dao.getCategoriasIdsForArticulo(articuloId)

    // Utility function: obtener IDs de artículos para una categoría
    fun getArticulosIdsForCategoria(categoriaId: Int): List<Int> =
        dao.getArticulosIdsForCategoria(categoriaId)
}