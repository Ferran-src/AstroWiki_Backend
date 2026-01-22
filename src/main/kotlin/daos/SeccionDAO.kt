package org.example.daos



import org.example.models.Seccion
import org.example.database.Secciones
import org.example.database.Usuarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object SeccionDAO {

    fun findAllWithCreator(): List<Seccion> = transaction {

        Secciones.join(Usuarios, JoinType.INNER, additionalConstraint = { Secciones.creadorId eq Usuarios.id })
            .selectAll()
            .map { row ->
                rowToSeccion(row)
            }.toList()
    }

    fun findByIdWithCreator(id: Int): Seccion? = transaction {
        Secciones.join(Usuarios, JoinType.INNER, additionalConstraint = { Secciones.creadorId eq Usuarios.id })
            .selectAll().where { Secciones.id eq id }
            .firstOrNull()?.let { row ->
                rowToSeccion(row)
            }
    }

    fun create(seccion: Seccion): Seccion = transaction {
        val insertedId = Secciones.insert {
            it[titulo] = seccion.titulo
            it[descripcion] = seccion.descripcion
            it[creadorId] = seccion.creadorId!!
        } get Secciones.id

        seccion.copy(idSeccion = insertedId.value)
    }

    fun update(id: Int, seccion: Seccion): Boolean = transaction {
        val updatedRows = Secciones.update({ Secciones.id eq id }) {
            it[titulo] = seccion.titulo
            it[descripcion] = seccion.descripcion
        }
        updatedRows > 0
    }

    fun delete(id: Int): Boolean = transaction {
        val deletedRows = Secciones.deleteWhere { Secciones.id eq id }
        deletedRows > 0
    }


    private fun rowToSeccion(row: ResultRow): Seccion {
        return Seccion(
            idSeccion = row[Secciones.id].value,
            titulo = row[Secciones.titulo],
            descripcion = row[Secciones.descripcion],
            fechaCreacion = row[Secciones.fechaCreacion].toString(),
            creadorId = row[Usuarios.id].value,
            imagen = null

        )
    }
}