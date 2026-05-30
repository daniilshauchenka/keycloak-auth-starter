package by.softclub.keycloak.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

class KeycloakAuthAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    OAuth2ResourceServerAutoConfiguration.class,
                    KeycloakAuthAutoConfiguration.class))
            .withPropertyValues("spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test");

    @Test
    void shouldCreateSecurityBeans() {
        contextRunner.withBean(JwtDecoder.class, () -> mock(JwtDecoder.class)).run(context -> {
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
            assertThat(context).hasBean("keycloakJwtAuthConverter");
            assertThat(context).hasBean("keycloakSecurityFilterChain");
        });
    }

    @Test
    void shouldBackOffWhenUserProvidesSecurityFilterChain() {
        contextRunner
                .withBean(JwtDecoder.class, () -> mock(JwtDecoder.class))
                .withBean("customChain", SecurityFilterChain.class, () -> mock(SecurityFilterChain.class))
                .run(context -> assertThat(context).hasBean("customChain"));
    }
}
