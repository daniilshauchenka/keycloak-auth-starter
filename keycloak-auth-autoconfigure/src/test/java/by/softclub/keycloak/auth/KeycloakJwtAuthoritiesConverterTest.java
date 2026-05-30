package by.softclub.keycloak.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtAuthoritiesConverterTest {

    @Test
    void shouldExtractRealmAndResourceRoles() {
        KeycloakJwtAuthoritiesConverter converter =
                new KeycloakJwtAuthoritiesConverter(KeycloakAuthProperties.RoleSource.BOTH, "leasing-api");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("admin")))
                .claim("resource_access", Map.of("leasing-api", Map.of("roles", List.of("manager"))))
                .build();

        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_admin", "ROLE_manager");
    }
}
