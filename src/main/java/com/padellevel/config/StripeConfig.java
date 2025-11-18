package com.padellevel.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para la integración con Stripe Payment API.
 *
 * Inicializa la API de Stripe con las claves configuradas en application.properties.
 * Soporta modo test (desarrollo) y modo producción.
 */
@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.public.key}")
    private String publicKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.enabled:false}")
    private boolean stripeEnabled;

    @PostConstruct
    public void init() {
        if (stripeEnabled) {
            Stripe.apiKey = apiKey;
            logger.info("Stripe API inicializada - Modo: {}",
                       apiKey.startsWith("sk_test_") ? "TEST" : "PRODUCCIÓN");
        } else {
            logger.info("Stripe está deshabilitado en este entorno");
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public boolean isStripeEnabled() {
        return stripeEnabled;
    }
}
