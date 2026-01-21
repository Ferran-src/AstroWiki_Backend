package org.example.services

import org.example.daos.CategoriaDAO
import org.example.models.Categoria

class CategoriaService {

    private val dao: CategoriaDAO = CategoriaDAO

    fun getAll(): List<Categoria> = dao.getAll()

    fun getById(id: Int): Categoria? = dao.getById(id)

    fun create(categoria: Categoria): Int = dao.create(categoria)

    fun update(id: Int, categoria: Categoria): Boolean = dao.update(id, categoria)

    fun delete(id: Int): Boolean = dao.delete(id)

    fun getByNombre(nombre: String): Categoria? = dao.getByNombre(nombre)
}