package com.library.prestamo_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "prestamos",
        indexes = {
                @Index(
                        name = "idx_prestamo_usuario",
                        columnList = "usuario_id"
                ),
                @Index(
                        name = "idx_prestamo_copia",
                        columnList = "copia_id"
                ),
                @Index(
                        name = "idx_prestamo_estado",
                        columnList = "estado"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Identificador del usuario proveniente de user-service.
     *
     * No se utiliza @ManyToOne porque user-service posee
     * una base de datos independiente.
     */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /*
     * Identificador de la copia proveniente de copia-service.
     *
     * No se utiliza relación JPA porque la copia pertenece
     * a otro microservicio y a otra base de datos.
     */
    @Column(name = "copia_id", nullable = false)
    private Long copiaId;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    /*
     * Queda null mientras el préstamo no haya sido devuelto.
     */
    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoPrestamo estado;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @PrePersist
    public void antesDeGuardar() {
        LocalDateTime ahora = LocalDateTime.now();

        creadoEn = ahora;
        actualizadoEn = ahora;

        if (estado == null) {
            estado = EstadoPrestamo.ACTIVO;
        }
    }

    @PreUpdate
    public void antesDeActualizar() {
        actualizadoEn = LocalDateTime.now();
    }
}