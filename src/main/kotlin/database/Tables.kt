package org.example.database


import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

//Configuracion de las tablas para exposed
object Usuarios : Table("usuarios") {
    val id = integer("id_usuario").autoIncrement().entityId()
    val nombreUsuario = varchar("nombre_usuario", 50).uniqueIndex()
    val correo = varchar("correo", 100).uniqueIndex()
    val contraseña = varchar("contraseña", 255) // Considera hashing
    val fechaRegistro = timestamp("fecha_registro").defaultExpression(CurrentTimestamp)
    val rol = varchar("rol", 10).default("lector")

    override val primaryKey = PrimaryKey(id)
}


object Articulos : Table("articulos") {
    val id = integer("id_articulo").autoIncrement().entityId()
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
    val fechaUltimaEdicion = timestamp("fecha_ultima_edicion").defaultExpression(CurrentTimestamp)
    val estado = varchar("estado", 10).default("activo")
    override val primaryKey = PrimaryKey(Usuarios.id)

}

object Categorias : Table("categorias") {
    val id = integer("id_categoria").autoIncrement().entityId()
    val nombreCategoria = varchar("nombre_categoria", 100).uniqueIndex()
    val descripcion = text("descripcion").nullable()
    override val primaryKey = PrimaryKey(id)

}

object ArticulosCategorias : Table("articulos_categorias") {
    val articuloId = integer("articulo_id").references(Articulos.id, onDelete = ReferenceOption.CASCADE)
    val categoriaId = integer("categoria_id").references(Categorias.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(articuloId, categoriaId)
}

object Imagenes : Table("imagenes") {
    val id = integer("id_imagen").autoIncrement().entityId()
    val titulo = varchar("titulo", 255).nullable()
    val descripcion = text("descripcion").nullable()
    val url = varchar("url", 500) // NOT NULL
    val fechaSubida = timestamp("fecha_subida").defaultExpression(CurrentTimestamp)
    val articuloId = integer("articulo_id").references(Articulos.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)

}

object Secciones : Table("secciones") {
     val id = integer("id_seccion").autoIncrement().entityId()
    val titulo = varchar("titulo", 255)
    val descripcion = text("descripcion").nullable()
    val creadorId = integer("creador_id").references(Usuarios.id)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

}

object Posts : Table("posts") {
     val id = integer("id_post").autoIncrement().entityId()
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val imagen = varchar("imagen", 255)
    val like = bool("like")
    val autorId = integer("autor_id").references(Usuarios.id)
    val seccionId = integer("seccion_id").references(Secciones.id, onDelete = ReferenceOption.CASCADE)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

}

object Comentarios : Table("comentarios") {
     val id = integer("id_comentario").autoIncrement().entityId()
    val contenido = text("contenido") // NOT NULL
    val autorId = integer("autor_id").references(Usuarios.id)
    val postId = integer("post_id").references(Posts.id, onDelete = ReferenceOption.CASCADE)
    val comentarioPadreId = integer("comentario_padre_id").references(id, onDelete = ReferenceOption.CASCADE)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

}

object SeguimientosSecciones : Table("seguimientos_secciones") {
    val usuarioId = integer("usuario_id").references(Usuarios.id, onDelete = ReferenceOption.CASCADE)
    val seccionId = integer("seccion_id").references(Secciones.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(usuarioId, seccionId)
}