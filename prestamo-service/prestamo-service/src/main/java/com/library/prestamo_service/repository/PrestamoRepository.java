package com.library.prestamo_service.repository;

import com.library.prestamo_service.model.EstadoPrestamo;
import com.library.prestamo_service.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /**
     * Comprueba si una copia posee un préstamo en alguno
     * de los estados recibidos.
     *
     * Se utilizará para impedir que una misma copia tenga
     * más de un préstamo ACTIVO o ATRASADO.
     */
    boolean existsByCopiaIdAndEstadoIn(
            Long copiaId,
            Collection<EstadoPrestamo> estados
    );

    /**
     * Cuenta cuántos préstamos vigentes posee un usuario.
     *
     * Permitirá aplicar posteriormente un límite máximo
     * de préstamos simultáneos.
     */
    long countByUsuarioIdAndEstadoIn(
            Long usuarioId,
            Collection<EstadoPrestamo> estados
    );

    /**
     * Busca todo el historial de préstamos de un usuario,
     * comenzando por los más recientes.
     */
    List<Prestamo> findByUsuarioIdOrderByFechaPrestamoDesc(
            Long usuarioId
    );

    /**
     * Busca todo el historial de préstamos asociado
     * a una copia concreta.
     */
    List<Prestamo> findByCopiaIdOrderByFechaPrestamoDesc(
            Long copiaId
    );

    /**
     * Busca préstamos según su estado.
     *
     * Los resultados se ordenan por la fecha de vencimiento
     * más cercana.
     */
    List<Prestamo> findByEstadoOrderByFechaVencimientoAsc(
            EstadoPrestamo estado
    );

    /**
     * Busca préstamos de un estado determinado cuya fecha
     * de vencimiento sea anterior a la fecha recibida.
     *
     * Se utilizará para encontrar préstamos ACTIVO
     * que ya deberían pasar a ATRASADO.
     */
    List<Prestamo>
    findByEstadoAndFechaVencimientoBeforeOrderByFechaVencimientoAsc(
            EstadoPrestamo estado,
            LocalDate fecha
    );
}