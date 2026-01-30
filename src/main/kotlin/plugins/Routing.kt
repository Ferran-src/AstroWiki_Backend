package org.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserHashedTableAuth
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respondText
import io.ktor.util.getDigestFunction
import org.example.routes.*

const val JWT_SECRET = "your-super-secret-jwt-key" // Usa una clave más segura y guárdala en variables de entorno
const val JWT_ISSUER = "AstroWiki"
const val JWT_AUDIENCE = "AstroWiki-users"
const val JWT_REALM = "AstroWiki API"

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JWT_REALM
            verifier(
                JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .withIssuer(JWT_ISSUER)
                    .withAudience(JWT_AUDIENCE)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JWT_AUDIENCE)) {
                    UserIdPrincipal(credential.payload.subject)
                } else {
                    null
                }
            }
        }
    }
}

fun Application.configureRouting() {

    val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }

    val hashedUserTable = UserHashedTableAuth(
        table = mapOf(
            "jetbrains" to digestFunction("foobar"),
            "admin" to digestFunction("password")
        ),
        digester = digestFunction
    )


//    install(Authentication) {
//        basic("auth-basic") {
//            realm = "Access to the '/' path"
//            validate { credentials ->
//                hashedUserTable.authenticate(credentials)
//            }
//        }
//    }

    routing {
        route("/api/v1") {
           articuloRoutes()
            usuarioRoutes()
            categoriaRoutes()
            articuloCategoriaRoutes()
            comentarioRoutes()
            postRoutes()
            postLikesRoutes()
            comentarioLikesRoutes()
            dynamicQueryRoutes()
            seccionRoutes()
            seguimientoSeccionesRoutes()
        }
        route("/imagen"){
            imagenRoutes()
        }
        // Ruta raíz u otras rutas que no necesiten un prefijo
        get("/") {
            call.respondText("Bienvenido a la API de Astrowiki!")
        }
    }
}