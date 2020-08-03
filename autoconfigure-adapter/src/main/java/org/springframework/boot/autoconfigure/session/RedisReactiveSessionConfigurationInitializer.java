package org.springframework.boot.autoconfigure.session;

import org.springframework.boot.autoconfigure.session.RedisReactiveSessionConfiguration.SpringBootRedisWebSessionConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.ReactiveSessionRepositoryCustomizer;
import org.springframework.session.data.redis.ReactiveRedisSessionRepository;

/**
 * {@link ApplicationContextInitializer} adapter for {@link RedisReactiveSessionConfiguration}.
 */
public class RedisReactiveSessionConfigurationInitializer {

    private final SessionProperties sessionProperties;
    private final RedisSessionProperties redisSessionProperties;

    public RedisReactiveSessionConfigurationInitializer(SessionProperties sessionProperties, RedisSessionProperties redisSessionProperties) {
        this.sessionProperties = sessionProperties;
        this.redisSessionProperties = redisSessionProperties;
    }

    public void initialize(GenericApplicationContext context) {
        SpringBootRedisWebSessionConfiguration springBootRedisWebSessionConfiguration = new SpringBootRedisWebSessionConfiguration();

        springBootRedisWebSessionConfiguration.customize(sessionProperties, redisSessionProperties);

        context.registerBean(ReactiveSessionRepository.class, () -> {
            springBootRedisWebSessionConfiguration.setRedisConnectionFactory(
                    context.getBeanProvider(ReactiveRedisConnectionFactory.class), // TODO use @SpringSessionRedisConnectionFactory annotation qualifier
                    context.getBeanProvider(ReactiveRedisConnectionFactory.class)
            );
            springBootRedisWebSessionConfiguration.setDefaultRedisSerializer(
                    (RedisSerializer<Object>) context.getBeanProvider(ResolvableType.forClassWithGenerics(RedisSerializer.class, Object.class)).getIfAvailable()
            );
            springBootRedisWebSessionConfiguration.setSessionRepositoryCustomizer(
                    context.getBeanProvider(ResolvableType.forClassWithGenerics(ReactiveSessionRepositoryCustomizer.class, ReactiveRedisSessionRepository.class))
            );
            return springBootRedisWebSessionConfiguration.sessionRepository();
        });
    }
}
