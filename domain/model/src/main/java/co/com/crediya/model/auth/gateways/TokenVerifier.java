package co.com.crediya.model.auth.gateways;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.exception.InvalidTokenException;

public interface TokenVerifier {
    /**
     * Validates a token and returns the identity it carries.
     *
     * @throws InvalidTokenException when the token is malformed, tampered with or expired
     */
    AuthenticatedUser verify(String token);
}
