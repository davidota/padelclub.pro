package com.padellevel.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejador de éxito para autenticación OAuth2.
 *
 * Se ejecuta después de que el usuario se autentica exitosamente con Google.
 * Puede personalizar la redirección y realizar acciones post-login.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    public OAuth2LoginSuccessHandler() {
        // URL por defecto después del login exitoso
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {

        logger.info("Login OAuth2 exitoso para usuario: {}", authentication.getName());

        // Obtener información del usuario OAuth2
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            logger.info("Usuario autenticado: username={}, email={}",
                       oauth2User.getName(), oauth2User.getEmail());

            // Aquí se pueden realizar acciones adicionales post-login:
            // - Registrar último login
            // - Enviar notificación de nuevo dispositivo
            // - Actualizar contadores
            // etc.
        }

        // Continuar con el flujo de autenticación
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
