package org.example.database

import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

import org.jetbrains.exposed.dao.id.IntIdTable


// Usar IntIdTable para entidades con ID primaria autoincremental
object Usuarios : IntIdTable("usuarios","id_usuario") {
    val nombreUsuario = varchar("nombre_usuario", 50).uniqueIndex()
    val correo = varchar("correo", 100).uniqueIndex()
    val contraseña = varchar("contraseña", 255)
    val fechaRegistro = timestamp("fecha_registro").defaultExpression(CurrentTimestamp)
    val rol = varchar("rol", 10).default("lector")
    val imagen = varchar("imagen", 500).nullable()
}

object Articulos : IntIdTable("articulos","id_articulo") {
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
    val fechaUltimaEdicion = timestamp("fecha_ultima_edicion").defaultExpression(CurrentTimestamp)
    val estado = varchar("estado", 10).default("activo").check { it inList listOf("activo", "borrado") } // Añadí check para simular ENUM
}

object Categorias : IntIdTable("categorias","id_categoria") { // Extiende IntIdTable
    val nombreCategoria = varchar("nombre_categoria", 100).uniqueIndex()
    val descripcion = text("descripcion").nullable()
}

object ArticulosCategorias : Table("articulos_categorias") {
    val articuloId = reference("articulo_id", Articulos, onDelete = ReferenceOption.CASCADE)
    val categoriaId = reference("categoria_id", Categorias, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(articuloId, categoriaId)
}

object Secciones : IntIdTable("secciones","id_seccion") {
    val titulo = varchar("titulo", 255)
    val descripcion = text("descripcion").nullable()
    val creadorId = reference("creador_id", Usuarios)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
    val imagen = varchar("imagen", 500).nullable()
}

object Posts : IntIdTable("posts","id_post") {
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val imagen = varchar("imagen", 500).nullable()
    val contadorLikes = integer("contador_likes").default(0)
    val contadorComentarios = integer("contador_comentarios").default(0)
    val autorId = reference("autor_id", Usuarios)
    val seccionId = reference("seccion_id", Secciones, onDelete = ReferenceOption.CASCADE)
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
}

object PostLikes: Table("posts_likes") {
    val postId = reference("post_id", Posts, onDelete = ReferenceOption.CASCADE) // FK
    val usuarioId = reference("usuario_id", Usuarios) // FK

    override val primaryKey = PrimaryKey(postId, usuarioId)
}

object Comentarios : IntIdTable("comentarios","id_comentario") {
    val contenido = text("contenido")
    val imagen = varchar("imagen", 500).nullable()
    val contadorLikes = integer("contador_likes").default(0)
    val autorId = reference("autor_id", Usuarios)
    val postId = reference("post_id", Posts, onDelete = ReferenceOption.CASCADE) //
    val comentarioPadreId = integer("comentario_padre_id").nullable() // FK a sí mismo
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
}

object ComentariosLikes : Table("comentarios_likes") {
    val comentarioId = reference("comentario_id", Comentarios, onDelete = ReferenceOption.CASCADE)
    val usuarioId = reference("usuario_id", Usuarios)

    override val primaryKey = PrimaryKey(usuarioId, comentarioId)
}

object SeguimientosSecciones : Table("seguimientos_secciones") {
    val usuarioId = reference("usuario_id", Usuarios, onDelete = ReferenceOption.CASCADE)
    val seccionId = reference("seccion_id", Secciones, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(usuarioId, seccionId)
}