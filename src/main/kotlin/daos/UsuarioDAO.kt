package org.example.daos

import org.example.models.Usuario // Asegúrate de tener tu data class Usuario
import org.example.database.Usuarios // Tu objeto Table de Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UsuarioDAO {
    fun findById(id: Int): Usuario? = transaction {
        Usuarios.selectAll().where { Usuarios.id eq id }.firstOrNull()?.let { row ->
            rowToUsuario(row)
        }
    }

    fun findByNombreUsuario(nombreUsuario: String): Usuario? = transaction {
        Usuarios.selectAll().where { Usuarios.nombreUsuario eq nombreUsuario }.firstOrNull()?.let { row ->
            rowToUsuario(row)
        }
    }

    fun findByCorreo(correo: String): Usuario? = transaction {
        Usuarios.selectAll().where { Usuarios.correo eq correo }.firstOrNull()?.let { row ->
            rowToUsuario(row)
        }
    }

    fun create(usuario: Usuario): Usuario = transaction {
        val insertedId = Usuarios.insert {
            it[nombreUsuario] = usuario.nombreUsuario
            it[correo] = usuario.correo
            it[contraseña] = usuario.contraseña // Asegúrate de hashear antes de llamar a create
            // fecha_registro se asigna por defecto en la BD
            it[rol] = usuario.rol
        } get Usuarios.id // Obtiene el ID generado

        // Retorna el usuario con el ID asignado por la base de datos
        usuario.copy(idUsuario = insertedId.value)
    }

    fun update(id: Int, usuario: Usuario): Boolean = transaction {
        // UPDATE usuarios SET nombre_usuario = ?, correo = ?, rol = ? WHERE id_usuario = ?;
        // (No se actualiza la contraseña ni la fecha de registro normalmente aquí)
        val updatedRows = Usuarios.update({ Usuarios.id eq id }) {
            it[nombreUsuario] = usuario.nombreUsuario
            it[correo] = usuario.correo
            // Importante: No se actualiza 'contraseña' en un update genérico de perfil
            // it[contraseña] = usuario.contraseña // <-- Normalmente NO
            it[rol] = usuario.rol // Asigna rol o valor por defecto
        }
        updatedRows > 0 // Retorna true si se actualizó al menos una fila
    }

    fun delete(id: Int): Boolean = transaction {
        // DELETE FROM usuarios WHERE id_usuario = ?;
        val deletedRows = Usuarios.deleteWhere { Usuarios.id eq id }
        deletedRows > 0 // Retorna true si se eliminó al menos una fila
    }

    // Función privada para mapear una fila de la base de datos a la data class Usuario
    private fun rowToUsuario(row: ResultRow): Usuario {
        return Usuario(
            idUsuario = row[Usuarios.id].value,
            nombreUsuario = row[Usuarios.nombreUsuario],
            correo = row[Usuarios.correo],
            contraseña = row[Usuarios.contraseña],
            fechaRegistro = row[Usuarios.fechaRegistro].toString(),
            rol = row[Usuarios.rol]
        )
    }
    fun updatePassword(id: Int, hashedPassword: String): Boolean = transaction {
        val updatedRows = Usuarios.update({ Usuarios.id eq id }) {
            it[contraseña] = hashedPassword
        }
        updatedRows > 0
    }

    // Método para actualizar perfil (sin tocar contraseña)
    fun updateProfile(id: Int, nombreUsuario: String, correo: String, rol: String?): Boolean = transaction {
        val updatedRows = Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.correo] = correo
            if (rol != null) it[Usuarios.rol] = rol
            // No se actualiza la contraseña
        }
        updatedRows > 0
    }

}