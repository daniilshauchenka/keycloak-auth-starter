package by.softclub.keycloak.auth;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ConfigurationProperties(prefix = "keycloak.auth")
public class KeycloakAuthProperties {

    private List<String> acceptedAudiences;

    private List<String> permitAll = List.of("/actuator/health", "/actuator/info");

    private RoleSource roleSource = RoleSource.BOTH;

    public enum RoleSource {
        REALM,
        RESOURCE,
        BOTH
    }

    private boolean addScopeAuthorities = true;

    private String resourceClientId;
}
