package com.padellevel.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pago realizado por un jugador.
 * Gestiona los pagos de inscripciones a torneos y pozos, incluyendo
 * información de transacciones, reembolsos y métodos de pago.
 */
@Entity
@Table(name = "pago")
public class Pago extends AbstractEntity {

    /**
     * Inscripción al torneo asociada a este pago (opcional).
     */
    @OneToOne
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

    /**
     * Inscripción al pozo asociada a este pago (opcional).
     */
    @OneToOne
    @JoinColumn(name = "inscripcion_pozo_id")
    private InscripcionPozo inscripcionPozo;

    /**
     * Monto del pago.
     */
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /**
     * Método de pago utilizado.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    /**
     * Estado del pago.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    /**
     * ID de la transacción del procesador de pagos.
     */
    private String transactionId;

    /**
     * Payment Intent ID de Stripe (si aplica).
     */
    private String paymentIntentId;

    /**
     * Fecha y hora en que se completó el pago.
     */
    private LocalDateTime fechaPago;

    /**
     * Fecha y hora del reembolso (si aplica).
     */
    private LocalDateTime fechaReembolso;

    /**
     * Detalles o notas adicionales sobre el pago.
     */
    @Column(length = 1000)
    private String detalles;

    // Constructores
    public Pago() {}

    // Getters y Setters
    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public InscripcionPozo getInscripcionPozo() {
        return inscripcionPozo;
    }

    public void setInscripcionPozo(InscripcionPozo inscripcionPozo) {
        this.inscripcionPozo = inscripcionPozo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public LocalDateTime getFechaReembolso() {
        return fechaReembolso;
    }

    public void setFechaReembolso(LocalDateTime fechaReembolso) {
        this.fechaReembolso = fechaReembolso;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}
