//maraco el jairo 
package com.library.copia_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "copias")
public class Copia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*atributo de codigo copia  */
    @Column(name = "codigo_copia", nullable = false, unique = true, length = 50)
    private String codigoCopia;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCopia estado;

    @Column(nullable = false, length = 100)
    private String ubicacion;

    @Column(length = 255)
    private String observacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoCopia.AVAILABLE;
        }
    }
}