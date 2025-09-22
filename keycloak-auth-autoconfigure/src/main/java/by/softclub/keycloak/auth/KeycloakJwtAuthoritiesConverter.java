package by.softclub.keycloak.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Достаёт роли из Keycloak:
 * - realm_access.roles -> ROLE_...
 * - resource_access.<client-id>.roles -> ROLE_...
 * client-id берётся из 'azp' или 'aud' (берём первый).
 */
public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final KeycloakAuthProperties.RoleSource roleSource;

    public KeycloakJwtAuthoritiesConverter(KeycloakAuthProperties.RoleSource roleSource) {
        this.roleSource = roleSource;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        if (roleSource == KeycloakAuthProperties.RoleSource.REALM || roleSource == KeycloakAuthProperties.RoleSource.BOTH) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            roles.addAll(extractRoles(realmAccess));
        }

        if (roleSource == KeycloakAuthProperties.RoleSource.RESOURCE || roleSource == KeycloakAuthProperties.RoleSource.BOTH) {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            roles.addAll(extractClientRoles(resourceAccess, resolveClientId(jwt)));
        }

        return roles.stream()
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    private static String resolveClientId(Jwt jwt) {
        String azp = jwt.getClaim("azp");
        if (azp != null) return azp;
        List<String> aud = jwt.getAudience();
        return (aud != null && !aud.isEmpty()) ? aud.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> extractRoles(Map<String, Object> access) {
        if (access == null) return List.of();
        Object roles = access.get("roles");
        if (roles instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).collect(Collectors.toSet());
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> extractClientRoles(Map<String, Object> resourceAccess, String clientId) {
        if (resourceAccess == null || clientId == null) return List.of();
        Object client = resourceAccess.get(clientId);
        if (client instanceof Map<?, ?> m) {
            Object roles = m.get("roles");
            if (roles instanceof Collection<?> c) {
                return c.stream().map(String::valueOf).collect(Collectors.toSet());
            }
        }
        return List.of();
    }
}