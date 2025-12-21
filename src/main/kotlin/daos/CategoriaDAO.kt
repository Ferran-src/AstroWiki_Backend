package org.example.daos

import org.jetbrains.exposed.sql.selectAll
import org.example.database.Categorias
import org.example.models.Categoria
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CategoriaDAO {

    fun getAll(): List<Categoria> = transaction {
        Categorias.selectAll().map { rowToCategoria(it) }
    }

    fun getById(id: Int): Categoria? = transaction {
        Categorias
            .selectAll().where { Categorias.id eq id }
            .map { rowToCategoria(it) }
            .singleOrNull()
    }

    fun getByNombre(nombre: String): Categoria? = transaction {
        Categorias
            .selectAll().where { Categorias.nombreCategoria eq nombre }
            .map { rowToCategoria(it) }
            .singleOrNull()
    }

    fun create(categoria: Categoria): Int = transaction {
        Categorias.insert {
            it[nombreCategoria] = categoria.nombreCategoria
            it[descripcion] = categoria.descripcion
        }[Categorias.id].value
    }

    fun update(id: Int, categoria: Categoria): Boolean = transaction {
        val updateCount = Categorias.update({ Categorias.id eq id }) { row ->
            row[nombreCategoria] = categoria.nombreCategoria
            row[descripcion] = categoria.descripcion
        }
        updateCount > 0
    }

    fun delete(id: Int): Boolean = transaction {
        val deleteCount = Categorias.deleteWhere { Categorias.id eq id }
        deleteCount > 0
    }

    // Método privado para convertir ResultRow a Categoria
    private fun rowToCategoria(row: ResultRow): Categoria = Categoria(
        idCategoria = row[Categorias.id].value,
        nombreCategoria = row[Categorias.nombreCategoria],
        descripcion = row[Categorias.descripcion]
    )
}