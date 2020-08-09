package org.springframework.boot.autoconfigure.security.oauth2.client.reactive;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;

import static java.util.Optional.ofNullable;

/**
 * {@link ApplicationContextInitializer} adapter for {@link ReactiveOAuth2ClientAutoConfiguration} and
 * {@link ReactiveOAuth2ClientConfigurations}.
 */
public class ReactiveOAuth2ClientConfigurationsInitializer {
    private OAuth2ClientProperties oAuth2ClientProperties;
    private ReactiveClientRegistrationRepository reactiveClientRegistrationRepository;

    public ReactiveOAuth2ClientConfigurationsInitializer(OAuth2ClientProperties oAuth2ClientProperties, ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
        this.reactiveClientRegistrationRepository = reactiveClientRegistrationRepository;
    }

    public void initialize(GenericApplicationContext context) {
        context.registerBean(ReactiveClientRegistrationRepository.class, () -> ofNullable(reactiveClientRegistrationRepository).orElseGet(() -> clientRegistrationRepository(oAuth2ClientProperties)));

        ReactiveOAuth2ClientConfigurations.ReactiveOAuth2ClientConfiguration clientConfiguration =
                new ReactiveOAuth2ClientConfigurations.ReactiveOAuth2ClientConfiguration();
        context.registerBean(ReactiveOAuth2AuthorizedClientService.class, () -> clientConfiguration.authorizedClientService(context.getBean(ReactiveClientRegistrationRepository.class)));
        context.registerBean(ServerOAuth2AuthorizedClientRepository.class, () -> clientConfiguration.authorizedClientRepository(context.getBean(ReactiveOAuth2AuthorizedClientService.class)));
    }

    public static ReactiveClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties oAuth2ClientProperties) {
        return new ReactiveOAuth2ClientConfigurations.ReactiveClientRegistrationRepositoryConfiguration().clientRegistrationRepository(oAuth2ClientProperties);
    }
}
