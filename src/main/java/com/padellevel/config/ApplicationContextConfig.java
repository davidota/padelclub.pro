package com.padellevel.config;

import com.padellevel.services.GamificacionService;
import com.padellevel.services.InscripcionService;
import com.padellevel.services.NotificacionService;
import com.padellevel.services.PagoService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Configuración del contexto de la aplicación.
 * Gestiona inyecciones lazy para evitar dependencias circulares.
 */
@Component
public class ApplicationContextConfig {

    private final NotificacionService notificacionService;
    private final GamificacionService gamificacionService;
    private final PagoService pagoService;
    private final InscripcionService inscripcionService;

    public ApplicationContextConfig(NotificacionService notificacionService,
                                   GamificacionService gamificacionService,
                                   PagoService pagoService,
                                   InscripcionService inscripcionService) {
        this.notificacionService = notificacionService;
        this.gamificacionService = gamificacionService;
        this.pagoService = pagoService;
        this.inscripcionService = inscripcionService;
    }

    /**
     * Configura las dependencias lazy después de que todos los beans estén creados.
     */
    @PostConstruct
    public void configureDependencies() {
        // Configurar inyección lazy para evitar dependencias circulares

        // GamificacionService necesita NotificacionService
        gamificacionService.setNotificacionService(notificacionService);

        // PagoService necesita NotificacionService y GamificacionService
        pagoService.setNotificacionService(notificacionService);
        pagoService.setGamificacionService(gamificacionService);

        // InscripcionService necesita NotificacionService y GamificacionService
        inscripcionService.setNotificacionService(notificacionService);
        inscripcionService.setGamificacionService(gamificacionService);
    }
}
