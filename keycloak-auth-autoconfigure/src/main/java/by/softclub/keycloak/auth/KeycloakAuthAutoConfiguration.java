package by.softclub.keycloak.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@AutoConfiguration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@ConditionalOnClass({HttpSecurity.class, JwtDecoder.class, SecurityFilterChain.class})
@EnableConfigurationProperties(KeycloakAuthProperties.class)
public class KeycloakAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "issuer-uri")
    public JwtDecoder jwtDecoder(org.springframework.core.env.Environment env, KeycloakAuthProperties props) {
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

    @Bean(name = "keycloakJwtAuthConverter")
    @ConditionalOnMissingBean(name = "keycloakJwtAuthConverter")
    public Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakJwtAuthConverter(
            KeycloakAuthProperties props) {

        KeycloakJwtAuthoritiesConverter roleConv =
                new KeycloakJwtAuthoritiesConverter(props.getRoleSource(), props.getResourceClientId());
        JwtGrantedAuthoritiesConverter scopeConv = new JwtGrantedAuthoritiesConverter();

        return jwt -> {
            Set<GrantedAuthority> auths = new HashSet<>();
            if (props.isAddScopeAuthorities()) {
                auths.addAll(scopeConv.convert(jwt));
            }
            auths.addAll(roleConv.convert(jwt));
            return new JwtAuthenticationToken(jwt, auths);
        };
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnProperty(prefix = "spring.security.oauth2.resourceserver.jwt", name = "issuer-uri")
    public SecurityFilterChain keycloakSecurityFilterChain(
            HttpSecurity http,
            KeycloakAuthProperties props,
            Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakJwtAuthConverter)
            throws Exception {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        http.authorizeHttpRequests(auth -> {
            props.getPermitAll().forEach(pattern -> auth.requestMatchers(matcherBuilder.matcher(pattern))
                    .permitAll());
            auth.anyRequest().authenticated();
        });
        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter)));
        http.csrf(csrf -> csrf.disable());
        return http.build();
    }
}
