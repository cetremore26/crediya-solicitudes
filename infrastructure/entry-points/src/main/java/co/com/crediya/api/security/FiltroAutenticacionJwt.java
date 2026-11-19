package co.com.crediya.api.security;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.exception.VerificacionTokensNoDisponibleException;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica la petición con el token Bearer. Un token inválido deja la petición anónima (la
 * cadena responde 401 si la ruta lo exige); si las claves de verificación no están disponibles
 * se responde 503 en el acto, porque el token puede ser válido.
 */
public class FiltroAutenticacionJwt extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FiltroAutenticacionJwt.class);
    private static final String PREFIJO_BEARER = "Bearer ";
    private static final String PREFIJO_ROL = "ROLE_";

    private final VerificadorTokens verificadorTokens;

    public FiltroAutenticacionJwt(VerificadorTokens verificadorTokens) {
        this.verificadorTokens = verificadorTokens;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            try {
                autenticar(header.substring(PREFIJO_BEARER.length()).trim());
            } catch (VerificacionTokensNoDisponibleException exception) {
                SecurityContextHolder.clearContext();
                RespuestasErrorSeguridad.verificacionNoDisponible(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(String token) {
        try {
            UsuarioAutenticado usuario = verificadorTokens.verificar(token);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            List.of(new SimpleGrantedAuthority(PREFIJO_ROL + usuario.rol().name()))));
        } catch (TokenInvalidoException exception) {
            LOGGER.warn("Token de acceso rechazado.");
            SecurityContextHolder.clearContext();
        }
    }
}
