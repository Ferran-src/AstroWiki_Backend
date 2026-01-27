package org.example.daos

import org.example.models.Usuario
import org.example.database.Usuarios
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
            it[contraseña] = usuario.contraseña
            it[rol] = usuario.rol
        } get Usuarios.id

        usuario.copy(idUsuario = insertedId.value, contraseña = "")
    }

    fun delete(id: Int): Boolean = transaction {
        val deletedRows = Usuarios.deleteWhere { Usuarios.id eq id }
        deletedRows > 0
    }

    private fun rowToUsuario(row: ResultRow): Usuario {
        return Usuario(
            idUsuario = row[Usuarios.id].value,
            nombreUsuario = row[Usuarios.nombreUsuario],
            correo = row[Usuarios.correo],
            contraseña = row[Usuarios.contraseña],
            fechaRegistro = row[Usuarios.fechaRegistro].toString(),
            rol = row[Usuarios.rol],
            imagen = row[Usuarios.imagen].takeIf { !it.isNullOrEmpty() }

        )
    }
    fun updatePassword(id: Int, hashedPassword: String): Boolean = transaction {
        val updatedRows = Usuarios.update({ Usuarios.id eq id }) {
            it[contraseña] = hashedPassword
        }
        updatedRows > 0
    }

    fun updateProfile(id: Int, nombreUsuario: String, correo: String, rol: String?, imagen: String?): Boolean = transaction {
        val updatedRows = Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.nombreUsuario] = nombreUsuario
            it[Usuarios.correo] = correo
            if (rol != null) it[Usuarios.rol] = rol
            if (imagen.isNullOrEmpty()) {
                it[Usuarios.imagen] = imagen
            }
        }
        updatedRows > 0
    }

}