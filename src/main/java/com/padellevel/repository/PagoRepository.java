package com.padellevel.repository;

import com.padellevel.data.EstadoPago;
import com.padellevel.data.Pago;
import com.padellevel.data.Inscripcion;
import com.padellevel.data.Torneo;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Pago entity operations.
 * Manages payment transactions for tournament inscriptions.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    /**
     * Find all payments for a specific inscription.
     *
     * @param inscripcion the inscription entity
     * @return list of payments for the specified inscription
     */
    List<Pago> findByInscripcion(Inscripcion inscripcion);

    /**
     * Find payments by inscription ID.
     *
     * @param inscripcionId the inscription ID
     * @return list of payments for the specified inscription
     */
    @Query("SELECT p FROM Pago p WHERE p.inscripcion.id = :inscripcionId ORDER BY p.fechaPago DESC")
    List<Pago> findByInscripcionId(@Param("inscripcionId") Long inscripcionId);

    /**
     * Find payments by status.
     *
     * @param estado the payment status
     * @return list of payments with the specified status
     */
    List<Pago> findByEstado(String estado);

    /**
     * Find payments by payment method.
     *
     * @param metodoPago the payment method
     * @return list of payments with the specified payment method
     */
    List<Pago> findByMetodoPago(String metodoPago);

    /**
     * Find a payment by transaction ID.
     *
     * @param transactionId the transaction ID
     * @return optional containing the payment if found
     */
    Optional<Pago> findByTransactionId(String transactionId);

    /**
     * Check if a transaction ID already exists.
     *
     * @param transactionId the transaction ID
     * @return true if the transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Find pending payments for a specific tournament.
     *
     * @param torneoId the tournament ID
     * @return list of pending payments
     */
    @Query("SELECT p FROM Pago p WHERE p.inscripcion.torneo.id = :torneoId AND p.estado = 'PENDIENTE'")
    List<Pago> findPendingByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find completed payments for a specific tournament.
     *
     * @param torneoId the tournament ID
     * @return list of completed payments
     */
    @Query("SELECT p FROM Pago p WHERE p.inscripcion.torneo.id = :torneoId AND p.estado = 'COMPLETADO'")
    List<Pago> findCompletedByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Calculate total payments for a tournament.
     *
     * @param torneoId the tournament ID
     * @return total amount of completed payments
     */
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.inscripcion.torneo.id = :torneoId AND p.estado = 'COMPLETADO'")
    Double calculateTotalByTorneoId(@Param("torneoId") Long torneoId);

    /**
     * Find payments by player ID.
     *
     * @param jugadorId the player ID
     * @return list of payments made by the specified player
     */
    @Query("SELECT p FROM Pago p WHERE p.inscripcion.jugador.id = :jugadorId ORDER BY p.fechaPago DESC")
    List<Pago> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Find a payment by Stripe Payment Intent ID.
     *
     * @param paymentIntentId the Stripe Payment Intent ID
     * @return optional containing the payment if found
     */
    Optional<Pago> findByStripePaymentIntentId(String paymentIntentId);

    /**
     * Find all payments for a specific tournament.
     *
     * @param torneo the tournament entity
     * @return list of payments for the specified tournament
     */
    List<Pago> findByTorneo(Torneo torneo);

    /**
     * Find all payments for a specific player.
     *
     * @param jugador the player/user entity
     * @return list of payments made by the specified player
     */
    List<Pago> findByJugador(User jugador);

    /**
     * Find payments by status (using enum).
     *
     * @param estado the payment status enum
     * @return list of payments with the specified status
     */
    List<Pago> findByEstado(EstadoPago estado);

    /**
     * Find payments for a tournament with a specific status.
     *
     * @param torneo the tournament entity
     * @param estado the payment status enum
     * @return list of payments matching the criteria
     */
    List<Pago> findByTorneoAndEstado(Torneo torneo, EstadoPago estado);
}
