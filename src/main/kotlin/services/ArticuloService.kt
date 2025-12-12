package org.example.services

import org.example.daos.ArticuloDAO
import org.example.models.Articulo

class ArticuloService(
    private val dao: ArticuloDAO = ArticuloDAO()
) {
    fun getAll() = dao.getAll()

    fun getById(id: Int) = dao.getById(id)

    fun create(articulo: Articulo) = dao.create(articulo)

    fun update(id: Int, articulo: Articulo) = dao.update(id, articulo)

    fun delete(id: Int) = dao.delete(id)
}
