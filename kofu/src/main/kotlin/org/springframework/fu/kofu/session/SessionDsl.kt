package org.springframework.fu.kofu.session

import org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfigurationInitializer
import org.springframework.boot.autoconfigure.session.RedisSessionProperties
import org.springframework.boot.autoconfigure.session.SessionInitializer
import org.springframework.boot.autoconfigure.session.SessionProperties
import org.springframework.context.support.GenericApplicationContext
import org.springframework.fu.kofu.AbstractDsl
import org.springframework.fu.kofu.ConfigurationDsl
import org.springframework.session.SaveMode
import java.time.Duration

/**
 * Kofu DSL for spring-session.
 *
 * Configure spring-session.
 *
 * Required dependencies can be retrieve using `org.springframework.boot:spring-boot-starter-session`.
 *
 * @author Ivan Skachkov
 */
class SessionDsl(private val init: SessionDsl.() -> Unit) : ConfigurationDsl({}) {

    override fun initialize(context: GenericApplicationContext) {
        super.initialize(context)
        init()
        SessionInitializer().initialize(context)
    }
}

/**
 * Configure spring-session.
 *
 * Requires `org.springframework.boot:spring-boot-starter-session` dependency.
 *
 * @sample org.springframework.fu.kofu.samples.sessionDsl
 * @author Ivan Skachkov
 */
fun ConfigurationDsl.session(dsl: SessionDsl.() -> Unit = {}) {
    SessionDsl(dsl).initialize(context)
}

/**
 * Kofu DSL for spring-session-data-redis.
 *
 * Configure spring-session-data-redis.
 *
 * Required dependencies can be retrieve using `org.springframework.session:spring-session-data-redis`.
 *
 * @see SessionProperties, RedisSessionProperties, org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession
 * @author Ivan Skachkov
 */
class ReactiveRedisDsl(
        private val init: ReactiveRedisDsl.() -> Unit,
        private val sessionProperties: SessionProperties,
        private val redisSessionProperties: RedisSessionProperties
) : AbstractDsl() {

    var maxInactiveIntervalInSeconds: Duration
        get() = sessionProperties.timeout
        set(value) {
            sessionProperties.timeout = value
        }

    /**
     * Namespace for keys used to store sessions.
     */
    var redisNamespace: String
        get() = redisSessionProperties.namespace
        set(value) {
            redisSessionProperties.namespace = value
        }

    /**
     * Sessions save mode. Determines how session changes are tracked and saved to the
     * session store.
     */
    var saveMode: SaveMode
        get() = redisSessionProperties.saveMode
        set(value) {
            redisSessionProperties.saveMode = value
        }

    override fun initialize(context: GenericApplicationContext) {
        super.initialize(context)
        init()
        RedisReactiveSessionConfigurationInitializer(sessionProperties, redisSessionProperties).initialize(context)
    }
}

/**
 * @see ReactiveRedisDsl
 */
fun SessionDsl.reactiveRedis(dsl: ReactiveRedisDsl.() -> Unit = {}) {
    val sessionProperties = configurationProperties(prefix = "spring.session", defaultProperties = SessionProperties(), strict = false)
    val redisSessionProperties = configurationProperties(prefix = "spring.session.redis", defaultProperties = RedisSessionProperties())
    ReactiveRedisDsl(dsl, sessionProperties, redisSessionProperties).initialize(context)
}
