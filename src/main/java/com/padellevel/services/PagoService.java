package com.padellevel.services;

import com.padellevel.config.StripeConfig;
import com.padellevel.data.*;
import com.padellevel.repository.PagoRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestionar pagos integrados con Stripe.
 *
 * Funcionalidades principales:
 * - Crear Payment Intents para inscripciones
 * - Confirmar pagos recibidos
 * - Gestionar reembolsos
 * - Consultar estado de pagos
 * - Sincronizar con webhook de Stripe
 */
@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final StripeConfig stripeConfig;
    private NotificacionService notificacionService; // Lazy injection to avoid circular dependency

    // Moneda por defecto (EUR para España)
    private static final String DEFAULT_CURRENCY = "eur";

    public PagoService(PagoRepository pagoRepository, StripeConfig stripeConfig) {
        this.pagoRepository = pagoRepository;
        this.stripeConfig = stripeConfig;
    }

    /**
     * Sets the NotificacionService (lazy injection to avoid circular dependency).
     */
    public void setNotificacionService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Crea un Payment Intent en Stripe para una inscripción de torneo.
     *
     * @param inscripcion La inscripción que requiere pago
     * @param torneo El torneo asociado
     * @param monto Monto a cobrar
     * @return El pago creado con el client_secret de Stripe
     * @throws StripeException Si hay error en la comunicación con Stripe
     */
    @Transactional
    public Pago crearPagoInscripcionTorneo(Inscripcion inscripcion, Torneo torneo, BigDecimal monto)
            throws StripeException {

        logger.info("Creando pago para inscripción {} - Torneo: {} - Monto: {}",
                   inscripcion.getId(), torneo.getNombre(), monto);

        // Validar que el torneo requiere pago
        if (torneo.getEsGratuito() != null && torneo.getEsGratuito()) {
            throw new IllegalArgumentException("El torneo es gratuito, no requiere pago");
        }

        // Crear entidad Pago
        Pago pago = new Pago();
        pago.setInscripcion(inscripcion);
        pago.setTorneo(torneo);
        pago.setJugador(inscripcion.getJugador());
        pago.setMonto(monto);
        pago.setMetodo(MetodoPago.STRIPE);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());

        // Si Stripe está deshabilitado, marcar como completado (modo test)
        if (!stripeConfig.isStripeEnabled()) {
            logger.warn("Stripe deshabilitado - Pago marcado como COMPLETADO automáticamente (modo test)");
            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setStripePaymentIntentId("test_pi_" + System.currentTimeMillis());
            pago.setComprobante("TEST_RECEIPT_" + System.currentTimeMillis());
            return pagoRepository.save(pago);
        }

        // Crear Payment Intent en Stripe
        try {
            PaymentIntent paymentIntent = crearPaymentIntent(monto, inscripcion, torneo);

            pago.setStripePaymentIntentId(paymentIntent.getId());
            pago.setStripeClientSecret(paymentIntent.getClientSecret());
            pago.setMoneda(paymentIntent.getCurrency());

            Pago pagoGuardado = pagoRepository.save(pago);
            logger.info("Payment Intent creado: {} - Client Secret enviado al frontend",
                       paymentIntent.getId());

            return pagoGuardado;

        } catch (StripeException e) {
            logger.error("Error al crear Payment Intent en Stripe", e);
            pago.setEstado(EstadoPago.FALLIDO);
            pago.setMensajeError(e.getMessage());
            pagoRepository.save(pago);
            throw e;
        }
    }

    /**
     * Crea un Payment Intent en Stripe para una inscripción de pozo.
     *
     * @param inscripcionPozo La inscripción al pozo
     * @param pozo El pozo asociado
     * @param monto Monto a cobrar
     * @return El pago creado con el client_secret de Stripe
     * @throws StripeException Si hay error en la comunicación con Stripe
     */
    @Transactional
    public Pago crearPagoInscripcionPozo(InscripcionPozo inscripcionPozo, Pozo pozo, BigDecimal monto)
            throws StripeException {

        logger.info("Creando pago para inscripción pozo {} - Pozo: {} - Monto: {}",
                   inscripcionPozo.getId(), pozo.getNombre(), monto);

        // Validar que el pozo requiere pago
        if (pozo.getEsGratuito() != null && pozo.getEsGratuito()) {
            throw new IllegalArgumentException("El pozo es gratuito, no requiere pago");
        }

        // Crear entidad Pago
        Pago pago = new Pago();
        pago.setInscripcionPozo(inscripcionPozo);
        pago.setPozo(pozo);
        pago.setTorneo(pozo.getTorneo());
        pago.setJugador(inscripcionPozo.getJugador());
        pago.setMonto(monto);
        pago.setMetodo(MetodoPago.STRIPE);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaPago(LocalDateTime.now());

        // Si Stripe está deshabilitado, marcar como completado (modo test)
        if (!stripeConfig.isStripeEnabled()) {
            logger.warn("Stripe deshabilitado - Pago marcado como COMPLETADO automáticamente (modo test)");
            pago.setEstado(EstadoPago.COMPLETADO);
            pago.setStripePaymentIntentId("test_pi_" + System.currentTimeMillis());
            pago.setComprobante("TEST_RECEIPT_" + System.currentTimeMillis());
            return pagoRepository.save(pago);
        }

        // Crear Payment Intent en Stripe
        try {
            PaymentIntent paymentIntent = crearPaymentIntentPozo(monto, inscripcionPozo, pozo);

            pago.setStripePaymentIntentId(paymentIntent.getId());
            pago.setStripeClientSecret(paymentIntent.getClientSecret());
            pago.setMoneda(paymentIntent.getCurrency());

            Pago pagoGuardado = pagoRepository.save(pago);
            logger.info("Payment Intent creado para pozo: {} - Client Secret enviado al frontend",
                       paymentIntent.getId());

            return pagoGuardado;

        } catch (StripeException e) {
            logger.error("Error al crear Payment Intent en Stripe para pozo", e);
            pago.setEstado(EstadoPago.FALLIDO);
            pago.setMensajeError(e.getMessage());
            pagoRepository.save(pago);
            throw e;
        }
    }

    /**
     * Confirma un pago cuando se recibe confirmación de Stripe (webhook).
     *
     * @param paymentIntentId El ID del Payment Intent de Stripe
     * @return El pago actualizado
     */
    @Transactional
    public Pago confirmarPago(String paymentIntentId) {
        logger.info("Confirmando pago con Payment Intent: {}", paymentIntentId);

        Optional<Pago> pagoOpt = pagoRepository.findByStripePaymentIntentId(paymentIntentId);

        if (pagoOpt.isEmpty()) {
            logger.error("No se encontró pago con Payment Intent: {}", paymentIntentId);
            throw new IllegalArgumentException("Pago no encontrado: " + paymentIntentId);
        }

        Pago pago = pagoOpt.get();

        if (pago.getEstado() == EstadoPago.COMPLETADO) {
            logger.warn("El pago {} ya estaba confirmado", pago.getId());
            return pago;
        }

        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setFechaPago(LocalDateTime.now());

        // Generar comprobante
        pago.setComprobante("RECEIPT_" + paymentIntentId);

        Pago pagoGuardado = pagoRepository.save(pago);

        // Actualizar estado de inscripción
        actualizarEstadoInscripcionTrasConfirmacion(pago);

        // Enviar notificación de pago confirmado
        if (notificacionService != null) {
            try {
                notificacionService.notificarPagoConfirmado(pagoGuardado);
            } catch (Exception e) {
                logger.error("Error al enviar notificación de pago confirmado", e);
                // No fallar la confirmación por error de notificación
            }
        }

        logger.info("Pago {} confirmado exitosamente", pago.getId());
        return pagoGuardado;
    }

    /**
     * Marca un pago como fallido cuando Stripe reporta un fallo.
     *
     * @param paymentIntentId El ID del Payment Intent de Stripe
     * @param mensajeError El mensaje de error reportado
     * @return El pago actualizado
     */
    @Transactional
    public Pago marcarPagoFallido(String paymentIntentId, String mensajeError) {
        logger.warn("Marcando pago como fallido - Payment Intent: {} - Error: {}",
                   paymentIntentId, mensajeError);

        Optional<Pago> pagoOpt = pagoRepository.findByStripePaymentIntentId(paymentIntentId);

        if (pagoOpt.isEmpty()) {
            logger.error("No se encontró pago con Payment Intent: {}", paymentIntentId);
            throw new IllegalArgumentException("Pago no encontrado: " + paymentIntentId);
        }

        Pago pago = pagoOpt.get();
        pago.setEstado(EstadoPago.FALLIDO);
        pago.setMensajeError(mensajeError);

        return pagoRepository.save(pago);
    }

    /**
     * Procesa un reembolso completo de un pago.
     *
     * @param pagoId El ID del pago a reembolsar
     * @param motivo Motivo del reembolso
     * @return El pago actualizado con estado REEMBOLSADO
     * @throws StripeException Si hay error al procesar el reembolso en Stripe
     */
    @Transactional
    public Pago procesarReembolsoCompleto(Long pagoId, String motivo) throws StripeException {
        logger.info("Procesando reembolso completo para pago: {} - Motivo: {}", pagoId, motivo);

        Pago pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + pagoId));

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos completados");
        }

        if (pago.getStripePaymentIntentId() == null) {
            throw new IllegalStateException("Pago no tiene Payment Intent de Stripe asociado");
        }

        // Si Stripe está deshabilitado, marcar como reembolsado directamente
        if (!stripeConfig.isStripeEnabled()) {
            logger.warn("Stripe deshabilitado - Reembolso marcado directamente (modo test)");
            pago.setEstado(EstadoPago.REEMBOLSADO);
            pago.setMensajeError("Reembolso test: " + motivo);
            return pagoRepository.save(pago);
        }

        try {
            // Crear reembolso en Stripe
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(pago.getStripePaymentIntentId())
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("motivo", motivo)
                .build();

            Refund refund = Refund.create(params);

            pago.setEstado(EstadoPago.REEMBOLSADO);
            pago.setStripeRefundId(refund.getId());
            pago.setMensajeError("Reembolso: " + motivo);

            // Actualizar estado de inscripción
            actualizarEstadoInscripcionTrasReembolso(pago);

            Pago pagoGuardado = pagoRepository.save(pago);
            logger.info("Reembolso completado - Refund ID: {}", refund.getId());

            return pagoGuardado;

        } catch (StripeException e) {
            logger.error("Error al procesar reembolso en Stripe", e);
            throw e;
        }
    }

    /**
     * Procesa un reembolso parcial de un pago.
     *
     * @param pagoId El ID del pago
     * @param montoReembolso Monto a reembolsar (menor al monto original)
     * @param motivo Motivo del reembolso
     * @return El pago actualizado
     * @throws StripeException Si hay error al procesar el reembolso en Stripe
     */
    @Transactional
    public Pago procesarReembolsoParcial(Long pagoId, BigDecimal montoReembolso, String motivo)
            throws StripeException {

        logger.info("Procesando reembolso parcial para pago: {} - Monto: {} - Motivo: {}",
                   pagoId, montoReembolso, motivo);

        Pago pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + pagoId));

        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos completados");
        }

        if (montoReembolso.compareTo(pago.getMonto()) >= 0) {
            throw new IllegalArgumentException(
                "El monto del reembolso parcial debe ser menor al monto original");
        }

        // Si Stripe está deshabilitado, marcar como reembolsado parcialmente
        if (!stripeConfig.isStripeEnabled()) {
            logger.warn("Stripe deshabilitado - Reembolso parcial marcado directamente (modo test)");
            pago.setMensajeError("Reembolso parcial test: " + motivo);
            return pagoRepository.save(pago);
        }

        try {
            // Convertir monto a centavos (Stripe usa centavos)
            long amountCents = montoReembolso.multiply(new BigDecimal(100)).longValue();

            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(pago.getStripePaymentIntentId())
                .setAmount(amountCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("motivo", motivo)
                .build();

            Refund refund = Refund.create(params);

            pago.setStripeRefundId(refund.getId());
            pago.setMensajeError("Reembolso parcial de " + montoReembolso + ": " + motivo);

            Pago pagoGuardado = pagoRepository.save(pago);
            logger.info("Reembolso parcial completado - Refund ID: {} - Monto: {}",
                       refund.getId(), montoReembolso);

            return pagoGuardado;

        } catch (StripeException e) {
            logger.error("Error al procesar reembolso parcial en Stripe", e);
            throw e;
        }
    }

    /**
     * Obtiene todos los pagos de un torneo.
     */
    public List<Pago> obtenerPagosPorTorneo(Torneo torneo) {
        return pagoRepository.findByTorneo(torneo);
    }

    /**
     * Obtiene todos los pagos de un jugador.
     */
    public List<Pago> obtenerPagosPorJugador(User jugador) {
        return pagoRepository.findByJugador(jugador);
    }

    /**
     * Obtiene pagos por estado.
     */
    public List<Pago> obtenerPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    /**
     * Calcula el total de ingresos de un torneo (solo pagos completados).
     */
    public BigDecimal calcularIngresosTorneo(Torneo torneo) {
        List<Pago> pagosCompletados = pagoRepository.findByTorneoAndEstado(torneo, EstadoPago.COMPLETADO);

        return pagosCompletados.stream()
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Crea un Payment Intent en Stripe para inscripción de torneo.
     */
    private PaymentIntent crearPaymentIntent(BigDecimal monto, Inscripcion inscripcion, Torneo torneo)
            throws StripeException {

        // Convertir monto a centavos (Stripe usa centavos)
        long amountCents = monto.multiply(new BigDecimal(100)).longValue();

        // Metadata para tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("inscripcion_id", inscripcion.getId().toString());
        metadata.put("torneo_id", torneo.getId().toString());
        metadata.put("torneo_nombre", torneo.getNombre());
        metadata.put("jugador_id", inscripcion.getJugador().getId().toString());
        metadata.put("jugador_email", inscripcion.getJugador().getEmail());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency(DEFAULT_CURRENCY)
            .setDescription("Inscripción a torneo: " + torneo.getNombre())
            .putAllMetadata(metadata)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        return PaymentIntent.create(params);
    }

    /**
     * Crea un Payment Intent en Stripe para inscripción de pozo.
     */
    private PaymentIntent crearPaymentIntentPozo(BigDecimal monto, InscripcionPozo inscripcionPozo, Pozo pozo)
            throws StripeException {

        // Convertir monto a centavos (Stripe usa centavos)
        long amountCents = monto.multiply(new BigDecimal(100)).longValue();

        // Metadata para tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("inscripcion_pozo_id", inscripcionPozo.getId().toString());
        metadata.put("pozo_id", pozo.getId().toString());
        metadata.put("pozo_nombre", pozo.getNombre());
        metadata.put("torneo_id", pozo.getTorneo().getId().toString());
        metadata.put("jugador_id", inscripcionPozo.getJugador().getId().toString());
        metadata.put("jugador_email", inscripcionPozo.getJugador().getEmail());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountCents)
            .setCurrency(DEFAULT_CURRENCY)
            .setDescription("Inscripción a pozo: " + pozo.getNombre())
            .putAllMetadata(metadata)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();

        return PaymentIntent.create(params);
    }

    /**
     * Actualiza el estado de la inscripción tras confirmación de pago.
     */
    private void actualizarEstadoInscripcionTrasConfirmacion(Pago pago) {
        if (pago.getInscripcion() != null) {
            Inscripcion inscripcion = pago.getInscripcion();
            inscripcion.setEstado(EstadoInscripcion.PAGADA);
            logger.info("Inscripción {} actualizada a estado PAGADA", inscripcion.getId());
        }

        if (pago.getInscripcionPozo() != null) {
            InscripcionPozo inscripcionPozo = pago.getInscripcionPozo();
            inscripcionPozo.setEstado(EstadoInscripcion.PAGADA);
            logger.info("InscripciónPozo {} actualizada a estado PAGADA", inscripcionPozo.getId());
        }
    }

    /**
     * Actualiza el estado de la inscripción tras reembolso.
     */
    private void actualizarEstadoInscripcionTrasReembolso(Pago pago) {
        if (pago.getInscripcion() != null) {
            Inscripcion inscripcion = pago.getInscripcion();
            inscripcion.setEstado(EstadoInscripcion.CANCELADA);
            logger.info("Inscripción {} actualizada a estado CANCELADA tras reembolso",
                       inscripcion.getId());
        }

        if (pago.getInscripcionPozo() != null) {
            InscripcionPozo inscripcionPozo = pago.getInscripcionPozo();
            inscripcionPozo.setEstado(EstadoInscripcion.CANCELADA);
            logger.info("InscripciónPozo {} actualizada a estado CANCELADA tras reembolso",
                       inscripcionPozo.getId());
        }
    }
}
