package com.padellevel.security;

import com.padellevel.data.Role;
import com.padellevel.data.User;
import com.padellevel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servicio personalizado para manejar autenticación OAuth2 con Google.
 *
 * Este servicio:
 * - Obtiene la información del usuario desde Google
 * - Crea un nuevo usuario si no existe
 * - Actualiza el usuario existente si ya está registrado
 * - Sincroniza datos del perfil (nombre, email, foto)
 * - Asigna roles por defecto (USER)
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Procesando autenticación OAuth2 con Google");

        // Obtener información del usuario desde Google
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extraer atributos del perfil
        Map<String, Object> attributes = oauth2User.getAttributes();
        String googleId = (String) attributes.get("sub"); // Google User ID
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String picture = (String) attributes.get("picture");
        Boolean emailVerified = (Boolean) attributes.get("email_verified");

        logger.info("Usuario OAuth2: email={}, name={}, googleId={}", email, name, googleId);

        // Buscar o crear usuario
        User user = processOAuth2User(googleId, email, name, givenName, familyName, picture, emailVerified);

        // Retornar un OAuth2User personalizado que incluye nuestro User
        return new CustomOAuth2User(oauth2User, user);
    }

    /**
     * Procesa el usuario OAuth2: crea uno nuevo o actualiza el existente.
     */
    private User processOAuth2User(String googleId, String email, String name,
                                   String givenName, String familyName,
                                   String picture, Boolean emailVerified) {

        // Buscar usuario existente por Google ID
        User user = userRepository.findByGoogleId(googleId).orElse(null);

        if (user != null) {
            // Usuario existente - actualizar información
            logger.info("Usuario existente encontrado: {}", user.getUsername());
            return updateExistingUser(user, email, name, givenName, familyName, picture);
        }

        // Buscar por email por si ya existe con otro método de autenticación
        user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // Usuario existe con email pero sin Google ID - vincular cuenta
            logger.info("Vinculando cuenta existente con Google: {}", user.getUsername());
            user.setGoogleId(googleId);
            return updateExistingUser(user, email, name, givenName, familyName, picture);
        }

        // Nuevo usuario - crear cuenta
        logger.info("Creando nuevo usuario desde Google OAuth2");
        return createNewUser(googleId, email, name, givenName, familyName, picture, emailVerified);
    }

    /**
     * Crea un nuevo usuario desde la información de Google.
     */
    private User createNewUser(String googleId, String email, String name,
                              String givenName, String familyName,
                              String picture, Boolean emailVerified) {

        User newUser = new User();

        // Información de Google
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setName(name != null ? name : givenName + " " + familyName);

        // Username generado desde email
        String username = generateUsernameFromEmail(email);
        newUser.setUsername(username);

        // No necesita password (autenticación OAuth2)
        newUser.setHashedPassword(null);

        // Foto de perfil de Google
        if (picture != null && !picture.isEmpty()) {
            newUser.setProfilePicture(picture);
        }

        // Usuario activo por defecto
        newUser.setActivo(true);

        // Asignar rol USER por defecto
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        newUser.setRoles(roles);

        // Guardar usuario
        User savedUser = userRepository.save(newUser);
        logger.info("Nuevo usuario creado: username={}, email={}", savedUser.getUsername(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * Actualiza un usuario existente con información de Google.
     */
    private User updateExistingUser(User user, String email, String name,
                                   String givenName, String familyName, String picture) {

        boolean updated = false;

        // Actualizar email si cambió
        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            updated = true;
        }

        // Actualizar nombre si cambió
        String fullName = name != null ? name : givenName + " " + familyName;
        if (fullName != null && !fullName.equals(user.getName())) {
            user.setName(fullName);
            updated = true;
        }

        // Actualizar foto de perfil si cambió
        if (picture != null && !picture.equals(user.getProfilePicture())) {
            user.setProfilePicture(picture);
            updated = true;
        }

        // Guardar cambios si hubo actualizaciones
        if (updated) {
            user = userRepository.save(user);
            logger.info("Usuario actualizado: {}", user.getUsername());
        }

        return user;
    }

    /**
     * Genera un username único desde el email.
     */
    private String generateUsernameFromEmail(String email) {
        // Tomar la parte antes del @
        String baseUsername = email.split("@")[0];

        // Verificar si ya existe
        if (!userRepository.findByUsername(baseUsername).isPresent()) {
            return baseUsername;
        }

        // Si existe, agregar número hasta encontrar uno disponible
        int counter = 1;
        String username = baseUsername + counter;
        while (userRepository.findByUsername(username).isPresent()) {
            counter++;
            username = baseUsername + counter;
        }

        return username;
    }
}
