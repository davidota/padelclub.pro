package com.padellevel.security;

import com.padellevel.data.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación personalizada de OAuth2User que envuelve nuestro User entity.
 *
 * Permite acceder tanto a los atributos de Google OAuth2 como a nuestro
 * User entity desde el contexto de seguridad.
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final User user;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertir roles del User a GrantedAuthority
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        // Retornar el username de nuestro User
        return user.getUsername();
    }

    /**
     * Obtiene nuestro User entity.
     */
    public User getUser() {
        return user;
    }

    /**
     * Obtiene el email del usuario.
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Obtiene el ID del usuario.
     */
    public Long getUserId() {
        return user.getId();
    }
}
