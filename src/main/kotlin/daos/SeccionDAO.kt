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
        // SELECT s.*, u.id_usuario, u.nombre_usuario, u.correo, u.fecha_registro, u.rol
        // FROM secciones s
        // JOIN usuarios u ON s.creador_id = u.id_usuario;
        Secciones.join(Usuarios, JoinType.INNER, additionalConstraint = { Secciones.creadorId eq Usuarios.id })
            .selectAll()
            .map { row ->
                rowToSeccion(row)
            }
    }

    fun findByIdWithCreator(id: Int): Seccion? = transaction {
        // SELECT s.*, u.id_usuario, u.nombre_usuario, u.correo, u.fecha_registro, u.rol
        // FROM secciones s
        // JOIN usuarios u ON s.creador_id = u.id_usuario
        // WHERE s.id_seccion = ?;
        Secciones.join(Usuarios, JoinType.INNER, additionalConstraint = { Secciones.creadorId eq Usuarios.id })
            .selectAll().where { Secciones.id eq id }
            .firstOrNull()?.let { row ->
                rowToSeccion(row)
            }
    }

    fun create(seccion: Seccion): Seccion = transaction {
        // INSERT INTO secciones (titulo, descripcion, creador_id) VALUES (?, ?, ?);
        val insertedId = Secciones.insert {
            it[titulo] = seccion.titulo
            it[descripcion] = seccion.descripcion
            it[creadorId] = seccion.creadorId
            // fecha_creacion se asigna por defecto en la BD
        } get Secciones.id

        // Retorna la sección con el ID asignado por la base de datos
        seccion.copy(idSeccion = insertedId.value)
    }

    fun update(id: Int, seccion: Seccion): Boolean = transaction {
        // UPDATE secciones SET titulo = ?, descripcion = ? WHERE id_seccion = ?;
        val updatedRows = Secciones.update({ Secciones.id eq id }) {
            it[titulo] = seccion.titulo
            it[descripcion] = seccion.descripcion
            // No se actualiza el creador ni la fecha de creación normalmente
        }
        updatedRows > 0
    }

    fun delete(id: Int): Boolean = transaction {
        // DELETE FROM secciones WHERE id_seccion = ?;
        val deletedRows = Secciones.deleteWhere { Secciones.id eq id }
        deletedRows > 0
    }

    // Función privada para mapear una fila de la base de datos (resultado del JOIN) a la data class Seccion
    // Esta función ahora mapea columnas de ambas tablas: Secciones y Usuarios
    private fun rowToSeccion(row: ResultRow): Seccion {
        return Seccion(
            idSeccion = row[Secciones.id].value,
            titulo = row[Secciones.titulo],
            descripcion = row[Secciones.descripcion],
            fechaCreacion = row[Secciones.fechaCreacion].toString(), // o LocalDateTime
            // Mapea los campos del creador desde la tabla Usuarios
            creadorId = row[Usuarios.id].value,
            nombreCreador = row[Usuarios.nombreUsuario],
            // Puedes incluir otros campos del usuario si es necesario
        )
    }
}