package by.softclub.keycloak.auth;

import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);
    private final List<String> acceptedAudiences;

    public AudienceValidator(List<String> acceptedAudiences) {
        this.acceptedAudiences = acceptedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (acceptedAudiences == null || acceptedAudiences.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }
        List<String> aud = token.getAudience();
        boolean ok = aud != null && aud.stream().anyMatch(acceptedAudiences::contains);
        return ok ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(error);
    }
}