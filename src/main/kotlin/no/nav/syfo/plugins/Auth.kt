package no.nav.syfo.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.time.Duration
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.logger
import no.nav.syfo.utils.EnvironmentVariables
import org.koin.ktor.ext.inject

fun Application.configureAuth() {
    if (environment.developmentMode) {
        configureDevelopmentAuth()
    } else {
        configureProductionAuth()
    }
}

fun Application.configureProductionAuth() {
    val config by inject<AuthConfiguration>()
    val env by inject<EnvironmentVariables>()

    install(Authentication) {
        jwt(name = "jwt") {
            verifier(config.jwkProvider, config.issuer)
            validate { credentials ->
                when {
                    isValidToken(credentials.payload, env) -> {
                        val email = credentials.payload.getClaim("preferred_username").asString()
                        requireNotNull(email) {
                            "Logged in user without preferred_username should not be possible. Are you wonderwalling?"
                        }
                        UserPrincipal(
                            email = email,
                        )
                    }
                    else -> {
                        unauthorized(credentials)
                    }
                }
            }
        }
    }
}

fun isValidToken(payload: Payload, env: EnvironmentVariables): Boolean {
    if (payload.issuer != env.jwtIssuer) {
        logger.warn("Something is wrong here with issuer")
        return false
    }
    if (!payload.audience.contains(env.clientId)) {
        logger.warn("Something is wrong here with audience")
        return false
    }
    return true
}

fun Application.configureDevelopmentAuth() {
    install(Authentication) {
        provider("local") {
            authenticate { context ->
                context.principal(UserPrincipal(email = "test.testerssen@nav.no"))
            }
        }
    }
}

data class UserPrincipal(val email: String) : Principal

class AuthConfiguration(
    val jwkProvider: JwkProvider,
    val issuer: String,
)

fun getProductionAuthConfig(env: EnvironmentVariables): AuthConfiguration {
    val jwkProvider =
        JwkProviderBuilder(URI.create(env.jwkKeysUrl).toURL())
            .cached(10, Duration.ofHours(24))
            .build()

    return AuthConfiguration(jwkProvider = jwkProvider, issuer = env.jwtIssuer)
}

internal fun unauthorized(credentials: JWTCredential): UserPrincipal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}
