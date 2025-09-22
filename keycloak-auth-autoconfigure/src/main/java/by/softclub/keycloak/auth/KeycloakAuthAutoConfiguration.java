package by.softclub.keycloak.auth;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass({HttpSecurity.class, JwtDecoder.class})
@EnableConfigurationProperties(KeycloakAuthProperties.class)
public class KeycloakAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "issuer-uri")
    public JwtDecoder jwtDecoder(org.springframework.core.env.Environment env,
        KeycloakAuthProperties props) {

        String issuer = env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> validator = withIssuer;
        List<String> aud = props.getAcceptedAudiences();
        if (aud != null && !aud.isEmpty()) {
            validator = new DelegatingOAuth2TokenValidator<>(withIssuer, new AudienceValidator(aud));
        }
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
        KeycloakAuthProperties props) throws Exception {
        var converter = new KeycloakJwtAuthoritiesConverter(props.getRoleSource());

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(reg -> reg
                .requestMatchers(props.getPermitAll().toArray(String[]::new)).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtToken -> {
                    var authorities = converter.convert(jwtToken);
                    return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
                        jwtToken, authorities
                    );
                }))
            );

        return http.build();
    }
}
