package com.padellevel.controllers;

import com.padellevel.config.StripeConfig;
import com.padellevel.services.PagoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recibir webhooks de Stripe.
 *
 * Stripe envía eventos a este endpoint cuando ocurren cambios en los pagos:
 * - payment_intent.succeeded: Pago completado exitosamente
 * - payment_intent.payment_failed: Pago fallido
 * - charge.refunded: Reembolso procesado
 *
 * IMPORTANTE: Este endpoint debe estar expuesto públicamente para que Stripe pueda acceder.
 * En desarrollo local, usar herramientas como ngrok para exponerlo.
 *
 * Configurar webhook URL en Stripe Dashboard:
 * - Local: https://your-ngrok-url.ngrok.io/api/stripe/webhook
 * - Production: https://padelclub.pro/api/stripe/webhook
 */
@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final PagoService pagoService;
    private final StripeConfig stripeConfig;

    public StripeWebhookController(PagoService pagoService, StripeConfig stripeConfig) {
        this.pagoService = pagoService;
        this.stripeConfig = stripeConfig;
    }

    /**
     * Endpoint para recibir webhooks de Stripe.
     *
     * @param payload El cuerpo del webhook (JSON raw)
     * @param sigHeader La firma del webhook (header Stripe-Signature)
     * @return 200 OK si el webhook fue procesado, 400 si hay error
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        logger.info("Webhook recibido de Stripe");

        // Si Stripe está deshabilitado, ignorar webhooks
        if (!stripeConfig.isStripeEnabled()) {
            logger.warn("Stripe deshabilitado - Webhook ignorado");
            return ResponseEntity.ok("Stripe disabled - webhook ignored");
        }

        Event event;

        try {
            // Verificar la firma del webhook para seguridad
            event = Webhook.constructEvent(
                payload,
                sigHeader,
                stripeConfig.getWebhookSecret()
            );

        } catch (SignatureVerificationException e) {
            logger.error("Firma de webhook inválida", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error al procesar webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webhook error: " + e.getMessage());
        }

        // Procesar el evento según su tipo
        try {
            processWebhookEvent(event);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error al procesar evento de webhook: {}", event.getType(), e);
            // Retornar 200 para que Stripe no reintente (ya loggeamos el error)
            return ResponseEntity.ok("Error logged, will not retry");
        }
    }

    /**
     * Procesa el evento del webhook según su tipo.
     */
    private void processWebhookEvent(Event event) {
        logger.info("Procesando evento de Stripe: {}", event.getType());

        // Deserializar el objeto del evento
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            logger.warn("No se pudo deserializar el objeto del evento");
            return;
        }

        // Procesar según el tipo de evento
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded((PaymentIntent) stripeObject);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed((PaymentIntent) stripeObject);
                break;

            case "payment_intent.canceled":
                handlePaymentIntentCanceled((PaymentIntent) stripeObject);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
                break;

            default:
                logger.info("Evento no manejado: {}", event.getType());
        }
    }

    /**
     * Maneja el evento de pago exitoso.
     */
    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        logger.info("Pago exitoso - Payment Intent: {}", paymentIntent.getId());

        try {
            pagoService.confirmarPago(paymentIntent.getId());
            logger.info("Pago confirmado en base de datos");

        } catch (Exception e) {
            logger.error("Error al confirmar pago en base de datos", e);
        }
    }

    /**
     * Maneja el evento de pago fallido.
     */
    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        logger.warn("Pago fallido - Payment Intent: {} - Error: {}",
                   paymentIntent.getId(),
                   paymentIntent.getLastPaymentError() != null
                       ? paymentIntent.getLastPaymentError().getMessage()
                       : "Unknown");

        try {
            String mensajeError = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Pago rechazado";

            pagoService.marcarPagoFallido(paymentIntent.getId(), mensajeError);
            logger.info("Pago marcado como fallido en base de datos");

        } catch (Exception e) {
            logger.error("Error al marcar pago como fallido", e);
        }
    }

    /**
     * Maneja el evento de pago cancelado.
     */
    private void handlePaymentIntentCanceled(PaymentIntent paymentIntent) {
        logger.info("Pago cancelado - Payment Intent: {}", paymentIntent.getId());

        try {
            pagoService.marcarPagoFallido(paymentIntent.getId(), "Pago cancelado por el usuario");
            logger.info("Pago marcado como fallido (cancelado) en base de datos");

        } catch (Exception e) {
            logger.error("Error al marcar pago cancelado", e);
        }
    }

    /**
     * Maneja el evento de cargo reembolsado.
     */
    private void handleChargeRefunded(Event event) {
        logger.info("Reembolso procesado - Event ID: {}", event.getId());

        // El reembolso ya fue procesado por PagoService.procesarReembolso()
        // Este evento es solo confirmación de Stripe
        logger.info("Reembolso confirmado por Stripe");
    }

    /**
     * Endpoint de health check para verificar que el webhook está accesible.
     */
    @GetMapping("/webhook/health")
    public ResponseEntity<String> webhookHealth() {
        return ResponseEntity.ok("Stripe webhook endpoint is healthy");
    }
}
