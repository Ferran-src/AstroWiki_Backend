package org.example.services

import org.example.models.Articulo
import org.example.database.Articulos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ArticuloService {

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
        }[Articulos.id].value  // Use .value to get Int from EntityID<Int>
    }

    fun update(id: Int, articulo: Articulo): Boolean = transaction {

        Articulos.update({ Articulos.id eq id }) {
            it[titulo] = articulo.titulo
            it[contenido] = articulo.contenido
            it[fechaUltimaEdicion] = fechaUltimaEdicion
            it[estado] = articulo.estado
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        Articulos.update({ Articulos.id eq id }) {
            it[estado] = "borrado"
            it[fechaUltimaEdicion] = LocalDateTime.now()
        } > 0
    }

    private fun rowToArticulo(row: ResultRow): Articulo {

        return Articulo(
            idArticulo = row[Articulos.id].value,  // Use .value to get Int
            titulo = row[Articulos.titulo],
            contenido = row[Articulos.contenido],
            fechaCreacion = row[Articulos.fechaCreacion],
            fechaUltimaEdicion = row[Articulos.fechaUltimaEdicion],
            estado = row[Articulos.estado]
        )
    }
}