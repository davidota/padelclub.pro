package com.padellevel.repository;

import com.padellevel.data.Logro;
import com.padellevel.data.LogroUsuario;
import com.padellevel.data.TipoLogro;
import com.padellevel.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LogroUsuario entity operations.
 * Manages the relationship between users and their unlocked achievements.
 */
@Repository
public interface LogroUsuarioRepository extends JpaRepository<LogroUsuario, Long>, JpaSpecificationExecutor<LogroUsuario> {

    /**
     * Verifica si un jugador ha desbloqueado un logro específico.
     *
     * @param jugador el jugador
     * @param logro el logro
     * @return true si el jugador tiene el logro
     */
    boolean existsByJugadorAndLogro(User jugador, Logro logro);

    /**
     * Obtiene el registro de logro de un jugador.
     *
     * @param jugador el jugador
     * @param logro el logro
     * @return Optional con el LogroUsuario si existe
     */
    Optional<LogroUsuario> findByJugadorAndLogro(User jugador, Logro logro);

    /**
     * Encuentra todos los logros desbloqueados por un jugador.
     *
     * @param jugador el jugador
     * @return lista de logros desbloqueados
     */
    List<LogroUsuario> findByJugadorOrderByFechaDesbloqueoDesc(User jugador);

    /**
     * Encuentra logros no vistos por un jugador.
     *
     * @param jugador el jugador
     * @param visto estado de visto
     * @return lista de logros no vistos
     */
    List<LogroUsuario> findByJugadorAndVistoOrderByFechaDesbloqueoDesc(User jugador, Boolean visto);

    /**
     * Cuenta los logros desbloqueados por un jugador.
     *
     * @param jugador el jugador
     * @return número de logros desbloqueados
     */
    long countByJugador(User jugador);

    /**
     * Encuentra logros de un jugador por tipo.
     *
     * @param jugador el jugador
     * @param tipo el tipo de logro
     * @return lista de logros del tipo especificado
     */
    @Query("SELECT lu FROM LogroUsuario lu WHERE lu.jugador = :jugador AND lu.logro.tipo = :tipo ORDER BY lu.fechaDesbloqueo DESC")
    List<LogroUsuario> findByJugadorAndTipo(@Param("jugador") User jugador, @Param("tipo") TipoLogro tipo);

    /**
     * Encuentra logros recientes desbloqueados en todo el sistema.
     *
     * @param fechaDesde fecha desde la cual buscar
     * @return lista de logros recientes
     */
    @Query("SELECT lu FROM LogroUsuario lu WHERE lu.fechaDesbloqueo >= :fechaDesde ORDER BY lu.fechaDesbloqueo DESC")
    List<LogroUsuario> findRecentUnlocks(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Encuentra los jugadores con más logros desbloqueados.
     *
     * @return lista de logros agrupados por jugador
     */
    @Query("SELECT lu.jugador, COUNT(lu) as total FROM LogroUsuario lu GROUP BY lu.jugador ORDER BY total DESC")
    List<Object[]> findTopPlayersByAchievements();

    /**
     * Cuenta logros de un tipo específico para un jugador.
     *
     * @param jugador el jugador
     * @param tipo el tipo de logro
     * @return número de logros del tipo
     */
    @Query("SELECT COUNT(lu) FROM LogroUsuario lu WHERE lu.jugador = :jugador AND lu.logro.tipo = :tipo")
    long countByJugadorAndTipo(@Param("jugador") User jugador, @Param("tipo") TipoLogro tipo);

    /**
     * Encuentra logros en progreso (no completos) para un jugador.
     *
     * @param jugador el jugador
     * @return lista de logros en progreso
     */
    @Query("SELECT lu FROM LogroUsuario lu WHERE lu.jugador = :jugador AND lu.progresoActual < lu.logro.meta ORDER BY lu.progresoActual DESC")
    List<LogroUsuario> findInProgressByJugador(@Param("jugador") User jugador);

    /**
     * Obtiene todos los jugadores que han desbloqueado un logro específico.
     *
     * @param logro el logro
     * @return lista de registros de logro
     */
    List<LogroUsuario> findByLogroOrderByFechaDesbloqueoAsc(Logro logro);
}
