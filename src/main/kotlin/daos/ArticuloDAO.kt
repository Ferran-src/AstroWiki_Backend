package org.example.daos

import org.example.database.Articulos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.example.models.Articulo
import org.jetbrains.exposed.sql.selectAll

class ArticuloDAO {

    fun getAll(): List<Articulo> = transaction {
        Articulos.selectAll().map { rowToArticulo(it) }
    }

    fun getById(id: Int): Articulo? = transaction {
        Articulos
            .selectAll().where { Articulos.id eq id }
            .map { rowToArticulo(it) }
            .singleOrNull()
    }

    fun create(articulo: Articulo): Int = transaction {

        Articulos.insert {
            it[titulo] = articulo.titulo
            it[contenido] = articulo.contenido
            it[fechaCreacion] = articulo.fechaCreacion
            it[fechaUltimaEdicion] = articulo.fechaUltimaEdicion
            it[estado] = articulo.estado
        }[Articulos.id]  // Use .value to get Int from EntityID<Int>
    }

    fun update(id: Int, articulo: Articulo): Boolean = transaction {
        Articulos.update({ Articulos.id eq id }) { row ->
            row[titulo] = articulo.titulo
            row[contenido] = articulo.contenido
            row[fechaUltimaEdicion] = articulo.fechaUltimaEdicion
            row[estado] = articulo.estado
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        Articulos.update({ Articulos.id eq id }) {
            it[estado] = "borrado"
        } > 0
    }

    // -----------------------
    // Convertir filas (ResultRow) -> Articulo (tu modelo)
    // -----------------------
    private fun rowToArticulo(row: ResultRow) = Articulo(
        idArticulo = row[Articulos.id],
        titulo = row[Articulos.titulo],
        contenido = row[Articulos.contenido],
        fechaCreacion = row[Articulos.fechaCreacion],
        fechaUltimaEdicion = row[Articulos.fechaUltimaEdicion],
        estado = row[Articulos.estado]
    )
}
