package org.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import org.example.models.Usuario
import java.util.Date

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

fun generateJWTToken(usuario: Usuario): String {
    val now = Date()
    val validity = Date(now.time + 30000)

    return JWT.create()
        .withSubject(usuario.idUsuario.toString())
        .withIssuer(JWT_ISSUER)
        .withAudience(JWT_AUDIENCE)
        .withClaim("userId", usuario.idUsuario)
        .withClaim("username", usuario.nombreUsuario)
        .withClaim("email", usuario.correo)
        .withClaim("role", usuario.rol)
        .withIssuedAt(now)
        .withExpiresAt(validity)
        .sign(Algorithm.HMAC256(JWT_SECRET))
}