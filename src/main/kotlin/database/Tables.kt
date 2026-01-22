package org.example.database

import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

import org.jetbrains.exposed.dao.id.IntIdTable


// Usar IntIdTable para entidades con ID primaria autoincremental
object Usuarios : IntIdTable("usuarios","id_usuario") { // Extiende IntIdTable
    val nombreUsuario = varchar("nombre_usuario", 50).uniqueIndex()
    val correo = varchar("correo", 100).uniqueIndex()
    val contraseña = varchar("contraseña", 255) // Considera hashing
    val fechaRegistro = timestamp("fecha_registro").defaultExpression(CurrentTimestamp)
    val rol = varchar("rol", 10).default("lector")
    val imagen = varchar("imagen", 500).nullable() // Aumenté el tamaño si es necesario
}

object Articulos : IntIdTable("articulos","id_articulo") { // Extiende IntIdTable
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp) // Cambié a timestamp
    val fechaUltimaEdicion = timestamp("fecha_ultima_edicion").defaultExpression(CurrentTimestamp) // Cambié a timestamp
    val estado = varchar("estado", 10).default("activo").check { it inList listOf("activo", "borrado") } // Añadí check para simular ENUM
}

object Categorias : IntIdTable("categorias","id_categoria") { // Extiende IntIdTable
    val nombreCategoria = varchar("nombre_categoria", 100).uniqueIndex()
    val descripcion = text("descripcion").nullable()
}

// Tabla intermedia para relaciones muchos-a-muchos
object ArticulosCategorias : Table("articulos_categorias") {
    val articuloId = reference("articulo_id", Articulos, onDelete = ReferenceOption.CASCADE)
    val categoriaId = reference("categoria_id", Categorias, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(articuloId, categoriaId)
}

object Secciones : IntIdTable("secciones","id_seccion") { // Extiende IntIdTable
    val titulo = varchar("titulo", 255)
    val descripcion = text("descripcion").nullable()
    val creadorId = reference("creador_id", Usuarios) // FK
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
    val imagen = varchar("imagen", 500).nullable() // Aumenté el tamaño si es necesario
}

object Posts : IntIdTable("posts","id_post") { // Extiende IntIdTable
    val titulo = varchar("titulo", 255)
    val contenido = text("contenido")
    val imagen = varchar("imagen", 500).nullable() // Aumenté el tamaño si es necesario
    val contadorLikes = integer("contador_likes").default(0)
    val contadorComentarios = integer("contador_comentarios").default(0) // Corregí nombre
    val autorId = reference("autor_id", Usuarios) // FK
    val seccionId = reference("seccion_id", Secciones, onDelete = ReferenceOption.CASCADE) // FK
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
}

object PostLikes: Table("posts_likes") {
    val postId = reference("post_id", Posts, onDelete = ReferenceOption.CASCADE) // FK
    val usuarioId = reference("usuario_id", Usuarios) // FK

    override val primaryKey = PrimaryKey(postId, usuarioId)
}

object Comentarios : IntIdTable("comentarios","id_comentario") { // Extiende IntIdTable
    val contenido = text("contenido") // NOT NULL implícito por defecto en text()
    val imagen = varchar("imagen", 500).nullable() // Aumenté el tamaño si es necesario
    val contadorLikes = integer("contador_likes").default(0)
    val autorId = reference("autor_id", Usuarios) // FK
    val postId = reference("post_id", Posts, onDelete = ReferenceOption.CASCADE) // FK
    val comentarioPadreId = integer("comentario_padre_id").nullable() // FK a sí mismo
    val fechaCreacion = timestamp("fecha_creacion").defaultExpression(CurrentTimestamp)
}

object ComentariosLikes : Table("comentarios_likes") {
    val comentarioId = reference("comentario_id", Comentarios, onDelete = ReferenceOption.CASCADE) // FK
    val usuarioId = reference("usuario_id", Usuarios) // FK

    override val primaryKey = PrimaryKey(usuarioId, comentarioId) // El orden puede ser indistinto
}

object SeguimientosSecciones : Table("seguimientos_secciones") {
    val usuarioId = reference("usuario_id", Usuarios, onDelete = ReferenceOption.CASCADE) // FK
    val seccionId = reference("seccion_id", Secciones, onDelete = ReferenceOption.CASCADE) // FK

    override val primaryKey = PrimaryKey(usuarioId, seccionId)
}