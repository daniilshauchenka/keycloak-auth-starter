package by.softclub.keycloak.auth;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "keycloak.auth")
public class KeycloakAuthProperties {

    /**
     * Дополнительно к стандартному spring.security.oauth2.resourceserver.jwt.issuer-uri можно настроить аудиторию для проверки 'aud' claim.
     */
    private List<String> acceptedAudiences;
    /** Разрешённые URL без аутентификации (доп. к actuator/health). */
    private List<String> permitAll = List.of("/actuator/health", "/actuator/info");

    /** Где искать роли: realm, resource или оба */
    private RoleSource roleSource = RoleSource.BOTH;

    public enum RoleSource {REALM, RESOURCE, BOTH}

}
