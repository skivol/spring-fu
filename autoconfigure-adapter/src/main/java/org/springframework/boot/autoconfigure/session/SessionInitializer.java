package org.springframework.boot.autoconfigure.session;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.config.annotation.web.server.SpringWebSessionConfiguration;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;

/**
 * {@link ApplicationContextInitializer} adapter for {@link SessionAutoConfiguration} and
 * {@link EnableSpringWebSession}.
 */
public class SessionInitializer {
    public void initialize(GenericApplicationContext context) {
        // SpringWebSessionConfiguration
        context.registerBean(WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME, WebSessionManager.class, () -> {
            SpringWebSessionConfiguration springWebSessionConfiguration = new SpringWebSessionConfiguration();
            WebSessionManager webSessionManager = springWebSessionConfiguration.webSessionManager(
                    (ReactiveSessionRepository<? extends Session>) context.getBeanProvider(
                            ResolvableType.forClassWithGenerics(ReactiveSessionRepository.class, Session.class)
                    ).getIfAvailable()
            );
            WebSessionIdResolver webSessionIdResolver = context.getBeanProvider(WebSessionIdResolver.class).getIfAvailable();
            if (webSessionIdResolver != null) {
                ((DefaultWebSessionManager)webSessionManager).setSessionIdResolver(webSessionIdResolver);
            }
            return webSessionManager;
        });
    }
}
