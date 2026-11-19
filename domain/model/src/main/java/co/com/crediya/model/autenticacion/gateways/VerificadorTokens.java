package co.com.crediya.model.autenticacion.gateways;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;

public interface VerificadorTokens {
    /**
     * Valida un token y devuelve la identidad que contiene.
     *
     * @throws TokenInvalidoException si el token está mal formado, fue alterado o expiró
     */
    UsuarioAutenticado verificar(String token);
}
