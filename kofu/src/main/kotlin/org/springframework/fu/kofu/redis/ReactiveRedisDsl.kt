package org.springframework.fu.kofu.redis

import org.springframework.boot.autoconfigure.data.redis.*
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.fu.kofu.ConfigurationDsl

/**
 * Kofu DSL for Reactive Redis configuration
 *
 * @author Waldemar Panas
 * @author Sebastien Deleuze
 */
@Suppress("UsePropertyAccessSyntax")
class ReactiveRedisDsl(private val init: ReactiveRedisDsl.() -> Unit, redisProperties: RedisProperties) : AbstractRedisDsl(redisProperties), LettuceRedisSupporter {

	private var lettuceInitializer: ApplicationContextInitializer<GenericApplicationContext>? = null

	override fun lettuce(dsl: LettuceDsl.() -> Unit) {
		lettuceInitializer = ApplicationContextInitializer {
			LettuceDsl(properties, dsl).initialize(it)
			LettuceRedisInitializer(properties).initialize(it)
		}
	}

	override fun initialize(context: GenericApplicationContext) {
		super.initialize(context)
		init()
		if (lettuceInitializer == null) lettuce()
		lettuceInitializer!!.initialize(context)
		RedisReactiveInitializer().initialize(context)
		ClusterInitializer(properties.cluster).initialize(context)
		SentinelInitializer(properties.sentinel).initialize(context)
	}
}

/**
 * Configure Reactive Redis support by registering 2 beans: `reactiveRedisTemplate` of
 * type `ReactiveRedisTemplate<Any, Any>` and `reactiveStringRedisTemplate` of type
 * `ReactiveStringRedisTemplate`.
 *
 * @see ReactiveRedisDsl
 */
fun ConfigurationDsl.reactiveRedis(dsl: ReactiveRedisDsl.() -> Unit = {}) {
	val redisProperties = configurationProperties<RedisProperties>(prefix = "spring.redis")
	ReactiveRedisDsl(dsl, redisProperties).initialize(context)
}