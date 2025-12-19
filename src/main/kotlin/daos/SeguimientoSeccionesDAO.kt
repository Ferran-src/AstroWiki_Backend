package org.example.daos

import org.example.models.SeguimientoSeccion
import org.example.database.SeguimientosSecciones
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object SeguimientoSeccionesDAO {
    fun findAll(): List<SeguimientoSeccion> = transaction {
        SeguimientosSecciones.selectAll().map { row ->
            rowToSeguimiento(row)
        }
    }

    fun findByUsuarioId(usuarioId: Int): List<SeguimientoSeccion> = transaction {
        SeguimientosSecciones.selectAll().where { SeguimientosSecciones.usuarioId eq usuarioId }.map { row ->
            rowToSeguimiento(row)
        }
    }

    fun findBySeccionId(seccionId: Int): List<SeguimientoSeccion> = transaction {
        SeguimientosSecciones.selectAll().where { SeguimientosSecciones.seccionId eq seccionId }.map { row ->
            rowToSeguimiento(row)
        }
    }

    fun findByUsuarioAndSeccion(usuarioId: Int, seccionId: Int): SeguimientoSeccion? = transaction {
        SeguimientosSecciones.selectAll()
            .where { SeguimientosSecciones.usuarioId eq usuarioId and (SeguimientosSecciones.seccionId eq seccionId) }.firstOrNull()?.let { row ->
            rowToSeguimiento(row)
        }
    }

    fun create(usuarioId: Int, seccionId: Int): SeguimientoSeccion = transaction {
        SeguimientosSecciones.insert {
            it[SeguimientosSecciones.usuarioId] = usuarioId
            it[SeguimientosSecciones.seccionId] = seccionId
        }
        SeguimientoSeccion(usuarioId = usuarioId, seccionId = seccionId)
    }

    fun delete(usuarioId: Int, seccionId: Int): Boolean = transaction {
        val deletedRows = SeguimientosSecciones.deleteWhere {
            SeguimientosSecciones.usuarioId eq usuarioId and (SeguimientosSecciones.seccionId eq seccionId)
        }
        deletedRows > 0
    }

    private fun rowToSeguimiento(row: ResultRow): SeguimientoSeccion {
        return SeguimientoSeccion(
            usuarioId = row[SeguimientosSecciones.usuarioId],
            seccionId = row[SeguimientosSecciones.seccionId]
        )
    }
}