package org.springframework.security.config.annotation.web.reactive;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.web.reactive.result.view.AbstractView;

import java.util.Collections;

/**
 * {@link ApplicationContextInitializer} adapter for {@link WebFluxSecurityConfiguration}.
 */
public class WebFluxSecurityInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	private final SecurityWebFilterChain webFilterChain;

	public WebFluxSecurityInitializer(SecurityWebFilterChain webFilterChain) {
		this.webFilterChain = webFilterChain;
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		WebFluxSecurityConfiguration configuration = new WebFluxSecurityConfiguration();
		configuration.context = context;
		configuration.setSecurityWebFilterChains(Collections.singletonList(webFilterChain));

		context.registerBean(
				"org.springframework.security.config.annotation.web.reactive.WebFluxSecurityConfiguration.WebFilterChainFilter",
				WebFilterChainProxy.class,
				configuration::springSecurityWebFilterChainFilter
		);
		context.registerBean(
				AbstractView.REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME,
				CsrfRequestDataValueProcessor.class,
				configuration::requestDataValueProcessor
		);
	}
}
