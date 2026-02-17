package org.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import org.example.models.Usuario
import java.util.Date

private val dotenv = dotenv(){
    filename = "astroEnv.env"
    directory = "${System.getProperty("user.dir")}"
}

val jwtSecret = dotenv["JWT_SECRET"]
val jwtIssuer = dotenv["JWT_ISSUER"]
val jwtAudience = dotenv["JWT_AUDIENCE"]
val jwtRealm = dotenv["JWT_REALM"]

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
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
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .withClaim("userId", usuario.idUsuario)
        .withClaim("username", usuario.nombreUsuario)
        .withClaim("email", usuario.correo)
        .withClaim("role", usuario.rol)
        .withIssuedAt(now)
        .withExpiresAt(validity)
        .sign(Algorithm.HMAC256(jwtSecret))
}