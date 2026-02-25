package org.example.daos

import org.example.database.Articulos
import org.jetbrains.exposed.sql.selectAll
import org.example.database.ArticulosCategorias
import org.example.database.Categorias
import org.example.models.ArticuloCategoria
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ArticuloCategoriaDAO {

    fun getAll(): List<ArticuloCategoria> = transaction {
        ArticulosCategorias.selectAll().map { rowToArticuloCategoria(it) }
    }

    fun getByArticuloId(articuloId: Int): List<ArticuloCategoria> = transaction {
        ArticulosCategorias
            .selectAll().where { ArticulosCategorias.articuloId eq articuloId }
            .map { rowToArticuloCategoria(it) }
    }

    fun getByCategoriaId(categoriaId: Int?): List<ArticuloCategoria> = transaction {
        ArticulosCategorias
            .selectAll().where { ArticulosCategorias.categoriaId eq categoriaId }
            .map { rowToArticuloCategoria(it) }
    }

    fun exists(articuloId: EntityID<Int>, categoriaId: EntityID<Int>): Boolean = transaction {
        ArticulosCategorias
            .selectAll().where {
                (ArticulosCategorias.articuloId eq articuloId) and
                        (ArticulosCategorias.categoriaId eq categoriaId)
            }
            .count() > 0
    }

    fun create(relacion: ArticuloCategoria): Boolean = transaction {
        try {
            // Verificar si ya existe
            if (exists(EntityID(relacion.articuloId, Articulos), EntityID( relacion.categoriaId, Categorias))) {
                return@transaction false
            }

            ArticulosCategorias.insert {
                it[articuloId] = relacion.articuloId
                it[categoriaId] = relacion.categoriaId
            }
            true
        } catch (e: Exception) {
            // Puede fallar por violación de claves foráneas
            false
        }
    }

    fun createBatch(relaciones: List<ArticuloCategoria>): Int = transaction {
        // Filtrar relaciones que ya existen
        val nuevasRelaciones = relaciones.filter { relacion ->
            !exists(EntityID(relacion.articuloId, Articulos), EntityID(relacion.categoriaId, Categorias))
        }

        // Insertar en lote
        if (nuevasRelaciones.isNotEmpty()) {
            ArticulosCategorias.batchInsert(nuevasRelaciones) { relacion ->
                this[ArticulosCategorias.articuloId] = relacion.articuloId
                this[ArticulosCategorias.categoriaId] = relacion.categoriaId
            }
        }

        nuevasRelaciones.size
    }

    fun delete(articuloId: Int, categoriaId: Int): Boolean = transaction {
        val deletedCount = ArticulosCategorias.deleteWhere {
            (ArticulosCategorias.articuloId eq articuloId) and
                    (ArticulosCategorias.categoriaId eq categoriaId)
        }
        deletedCount > 0
    }

    fun deleteByArticuloId(articuloId: EntityID<Int>): Int = transaction {
        ArticulosCategorias.deleteWhere { ArticulosCategorias.articuloId eq articuloId }
    }

    fun deleteByCategoriaId(categoriaId: Int): Int = transaction {
        ArticulosCategorias.deleteWhere { ArticulosCategorias.categoriaId eq categoriaId }
    }

    fun replaceCategoriasForArticulo(articuloId: EntityID<Int>, categoriasIds: List<EntityID<Int>>): Pair<Int, Int> = transaction {
        // 1. Eliminar categorías antiguas
        val deletedCount = deleteByArticuloId(articuloId)

        // 2. Agregar nuevas categorías
        val nuevasRelaciones = categoriasIds.map { categoriaId ->
            ArticuloCategoria(articuloId.value, categoriaId.value)
        }
        val createdCount = createBatch(nuevasRelaciones)

        Pair(deletedCount, createdCount)
    }

    fun getCategoriasIdsForArticulo(articuloId: Int): List<EntityID<Int>> = transaction {
        ArticulosCategorias
            .selectAll().where { ArticulosCategorias.articuloId eq articuloId }
            .map { it[ArticulosCategorias.categoriaId] }
    }

    fun getArticulosIdsForCategoria(categoriaId: Int): List<EntityID<Int>> = transaction {
        ArticulosCategorias
            .selectAll().where { ArticulosCategorias.categoriaId eq categoriaId }
            .map { it[ArticulosCategorias.articuloId] }
    }

    // Método privado para convertir ResultRow a ArticuloCategoria
    private fun rowToArticuloCategoria(row: ResultRow): ArticuloCategoria = ArticuloCategoria(
        articuloId = row[ArticulosCategorias.articuloId].value,
        categoriaId = row[ArticulosCategorias.categoriaId].value
    )
}