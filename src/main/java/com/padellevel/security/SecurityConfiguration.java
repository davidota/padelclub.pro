package com.padellevel.security;

import com.padellevel.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Importaciones necesarias
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de seguridad de Spring Security con soporte para OAuth2.
 *
 * Características:
 * - Autenticación tradicional con username/password
 * - Autenticación OAuth2 con Google
 * - Protección de rutas
 * - Integración con Vaadin Flow
 */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Permitir acceso a recursos estáticos y a la consola de H2
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/stripe/webhook")).permitAll() // Webhook de Stripe
            .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll() // OAuth2 callbacks
            .requestMatchers(new AntPathRequestMatcher("/login/oauth2/**")).permitAll() // OAuth2 login
            // Rutas de la API REST - acceso público para lectura
            .requestMatchers(new AntPathRequestMatcher("/api/v1/torneos/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/gamificacion/ranking/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/gamificacion/jugador/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/predicciones/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/clubes")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/clubes/*/buscar")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/api/v1/clubes/*/estadisticas")).permitAll()
            // WebSocket endpoints
            .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
            // Documentación de la API (Swagger/OpenAPI)
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
            // Otras rutas permitidas...
            // .anyRequest().authenticated() // Descomenta según tus necesidades
        );

        // Configurar CSRF para ignorar webhooks, consola y API REST
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            .ignoringRequestMatchers(new AntPathRequestMatcher("/api/stripe/webhook"))
            .ignoringRequestMatchers(new AntPathRequestMatcher("/api/v1/**")) // Ignorar CSRF para toda la API REST
        );

        // Permitir frames para la consola de H2
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions
                .disable()
            )
        );

        // Configurar OAuth2 Login
        http.oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)
            )
            .successHandler(oauth2LoginSuccessHandler)
            .defaultSuccessUrl("/", true)
            .failureUrl("/login?error=oauth2")
        );

        // Configuración de Vaadin
        super.configure(http);

        // Establecer la vista de inicio de sesión
        setLoginView(http, LoginView.class);
    }
}
