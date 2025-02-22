package com.padellevel.security;

import com.padellevel.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Importaciones necesarias
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

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
            // Otras rutas permitidas...
            // .anyRequest().authenticated() // Descomenta según tus necesidades
        );

        // Configurar CSRF para ignorar la consola de H2
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
        );

        // Permitir frames para la consola de H2
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions
                .disable()
            )
        );

        // Configuración de Vaadin
        super.configure(http);

        // Establecer la vista de inicio de sesión
        setLoginView(http, LoginView.class);
    }
}
