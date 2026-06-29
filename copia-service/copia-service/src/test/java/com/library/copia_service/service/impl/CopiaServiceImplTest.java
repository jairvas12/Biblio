
package com.library.copia_service.service.impl;

import com.library.copia_service.exception.RemoteServiceException;
import com.library.copia_service.client.BookClient;
import com.library.copia_service.dto.BookResponseDTO;
import com.library.copia_service.dto.CopiaRequestDTO;
import com.library.copia_service.dto.CopiaResponseDTO;
import com.library.copia_service.exception.BusinessException;
import com.library.copia_service.exception.ResourceNotFoundException;
import com.library.copia_service.mapper.CopiaMapper;
import com.library.copia_service.model.Copia;
import com.library.copia_service.model.EstadoCopia;
import com.library.copia_service.repository.CopiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopiaServiceImplTest {

    @Mock
    private CopiaRepository copiaRepository;

    @Mock
    private BookClient bookClient;

    private CopiaMapper copiaMapper;

    private CopiaServiceImpl copiaService;

    @BeforeEach
    void setUp() {
        copiaMapper = new CopiaMapper();

        copiaService = new CopiaServiceImpl(
                copiaRepository,
                bookClient,
                copiaMapper
        );
    }

    @Test
    void crearCopiaCorrectamente() {
        CopiaRequestDTO request = new CopiaRequestDTO(
                "COPY-010",
                2L,
                null,
                "Estante A-1",
                "Copia en buen estado"
        );

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.existsByCodigoCopia("COPY-010"))
                .thenReturn(false);

        when(copiaRepository.save(any(Copia.class)))
                .thenAnswer(invocation -> {
                    Copia copia = invocation.getArgument(0);

                    copia.setId(10L);
                    copia.setFechaRegistro(
                            LocalDateTime.of(
                                    2026,
                                    6,
                                    29,
                                    1,
                                    0
                            )
                    );

                    return copia;
                });

        CopiaResponseDTO resultado =
                copiaService.crearCopia(request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("COPY-010", resultado.getCodigoCopia());
        assertEquals(2L, resultado.getBookId());
        assertEquals(
                "Historia de Chile",
                resultado.getBookTitle()
        );
        assertEquals(
                "Francisco Antonio Encina",
                resultado.getBookAuthor()
        );
        assertEquals(
                EstadoCopia.AVAILABLE,
                resultado.getEstado()
        );
        assertEquals(
                "Estante A-1",
                resultado.getUbicacion()
        );
        assertEquals(
                "Copia en buen estado",
                resultado.getObservacion()
        );
        assertNotNull(resultado.getFechaRegistro());

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .existsByCodigoCopia("COPY-010");

        verify(copiaRepository)
                .save(any(Copia.class));
    }

    @Test
    void crearCopiaConSolicitudNulaLanzaBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> copiaService.crearCopia(null)
        );

        assertEquals(
                "La solicitud de copia no puede ser nula",
                exception.getMessage()
        );

        verifyNoInteractions(
                copiaRepository,
                bookClient
        );
    }

    @Test
    void crearCopiaConCodigoDuplicadoLanzaBusinessException() {
        CopiaRequestDTO request = new CopiaRequestDTO(
                "COPY-010",
                2L,
                null,
                "Estante A-1",
                "Código duplicado"
        );

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.existsByCodigoCopia("COPY-010"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> copiaService.crearCopia(request)
        );

        assertTrue(
                exception.getMessage().contains("COPY-010")
        );

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .existsByCodigoCopia("COPY-010");

        verify(copiaRepository, never())
                .save(any(Copia.class));
    }

    @Test
    void crearCopiaConEstadoInicialDistintoDeAvailableLanzaBusinessException() {
        CopiaRequestDTO request = new CopiaRequestDTO(
                "COPY-011",
                2L,
                EstadoCopia.LOANED,
                "Estante A-2",
                "Estado inicial inválido"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> copiaService.crearCopia(request)
        );

        assertEquals(
                "Toda copia nueva debe comenzar en estado AVAILABLE",
                exception.getMessage()
        );

        verifyNoInteractions(
                copiaRepository,
                bookClient
        );
    }

    @Test
    void buscarCopiaPorIdCorrectamente() {
        LocalDateTime fechaRegistro = LocalDateTime.of(
                2026,
                6,
                29,
                2,
                0
        );

        Copia copia = Copia.builder()
                .id(2L)
                .codigoCopia("COPY-002")
                .bookId(2L)
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-3")
                .observacion("Copia disponible")
                .fechaRegistro(fechaRegistro)
                .build();

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(copiaRepository.findById(2L))
                .thenReturn(Optional.of(copia));

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        CopiaResponseDTO resultado =
                copiaService.buscarCopiaPorId(2L);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals("COPY-002", resultado.getCodigoCopia());
        assertEquals(2L, resultado.getBookId());
        assertEquals(
                "Historia de Chile",
                resultado.getBookTitle()
        );
        assertEquals(
                "Francisco Antonio Encina",
                resultado.getBookAuthor()
        );
        assertEquals(
                EstadoCopia.AVAILABLE,
                resultado.getEstado()
        );
        assertEquals(
                "Estante A-3",
                resultado.getUbicacion()
        );
        assertEquals(
                fechaRegistro,
                resultado.getFechaRegistro()
        );

        verify(copiaRepository)
                .findById(2L);

        verify(bookClient)
                .buscarLibroPorId(2L);
    }

    @Test
    void buscarCopiaPorIdInexistenteLanzaResourceNotFoundException() {
        when(copiaRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> copiaService.buscarCopiaPorId(999L)
        );

        assertEquals(
                "No existe una copia con ID: 999",
                exception.getMessage()
        );

        verify(copiaRepository)
                .findById(999L);

        verifyNoInteractions(bookClient);
    }

    @Test
    void listarCopiasCorrectamente() {
        Copia copiaUno = Copia.builder()
                .id(1L)
                .codigoCopia("COPY-001")
                .bookId(2L)
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-1")
                .observacion("Primera copia")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                2,
                                10
                        )
                )
                .build();

        Copia copiaDos = Copia.builder()
                .id(2L)
                .codigoCopia("COPY-002")
                .bookId(2L)
                .estado(EstadoCopia.LOANED)
                .ubicacion("Estante A-2")
                .observacion("Segunda copia")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                2,
                                20
                        )
                )
                .build();

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                2
        );

        when(copiaRepository.findAll())
                .thenReturn(List.of(copiaUno, copiaDos));

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        List<CopiaResponseDTO> resultado =
                copiaService.listarCopias();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(
                "COPY-001",
                resultado.get(0).getCodigoCopia()
        );
        assertEquals(
                "COPY-002",
                resultado.get(1).getCodigoCopia()
        );
        assertEquals(
                "Historia de Chile",
                resultado.get(0).getBookTitle()
        );
        assertEquals(
                "Historia de Chile",
                resultado.get(1).getBookTitle()
        );

        verify(copiaRepository)
                .findAll();

        verify(bookClient)
                .buscarLibroPorId(2L);
    }

    @Test
    void listarCopiasPorLibroCorrectamente() {
        Copia copia = Copia.builder()
                .id(2L)
                .codigoCopia("COPY-002")
                .bookId(2L)
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-3")
                .observacion("Copia disponible")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                2,
                                30
                        )
                )
                .build();

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.findByBookId(2L))
                .thenReturn(List.of(copia));

        List<CopiaResponseDTO> resultado =
                copiaService.listarCopiasPorLibro(2L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(
                "COPY-002",
                resultado.get(0).getCodigoCopia()
        );
        assertEquals(
                2L,
                resultado.get(0).getBookId()
        );
        assertEquals(
                EstadoCopia.AVAILABLE,
                resultado.get(0).getEstado()
        );
        assertEquals(
                "Historia de Chile",
                resultado.get(0).getBookTitle()
        );

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .findByBookId(2L);
    }

    @Test
    void listarCopiasDisponiblesPorLibroCorrectamente() {
        Copia copiaDisponible = Copia.builder()
                .id(2L)
                .codigoCopia("COPY-002")
                .bookId(2L)
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-3")
                .observacion("Copia disponible")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                3,
                                0
                        )
                )
                .build();

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.findByBookIdAndEstado(
                2L,
                EstadoCopia.AVAILABLE
        )).thenReturn(List.of(copiaDisponible));

        List<CopiaResponseDTO> resultado =
                copiaService.listarCopiasDisponiblesPorLibro(2L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(
                "COPY-002",
                resultado.get(0).getCodigoCopia()
        );
        assertEquals(
                EstadoCopia.AVAILABLE,
                resultado.get(0).getEstado()
        );
        assertEquals(
                "Historia de Chile",
                resultado.get(0).getBookTitle()
        );

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .findByBookIdAndEstado(
                        2L,
                        EstadoCopia.AVAILABLE
                );
    }

    @Test
    void listarCopiasPorEstadoCorrectamente() {
        Copia copiaPrestada = Copia.builder()
                .id(3L)
                .codigoCopia("COPY-003")
                .bookId(2L)
                .estado(EstadoCopia.LOANED)
                .ubicacion("Estante A-4")
                .observacion("Copia prestada")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                3,
                                10
                        )
                )
                .build();

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(copiaRepository.findByEstado(EstadoCopia.LOANED))
                .thenReturn(List.of(copiaPrestada));

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        List<CopiaResponseDTO> resultado =
                copiaService.listarCopiasPorEstado(
                        EstadoCopia.LOANED
                );

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(
                "COPY-003",
                resultado.get(0).getCodigoCopia()
        );
        assertEquals(
                EstadoCopia.LOANED,
                resultado.get(0).getEstado()
        );
        assertEquals(
                "Historia de Chile",
                resultado.get(0).getBookTitle()
        );

        verify(copiaRepository)
                .findByEstado(EstadoCopia.LOANED);

        verify(bookClient)
                .buscarLibroPorId(2L);
    }

    @Test
    void contarCopiasDisponiblesPorLibroCorrectamente() {
        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                3
        );

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.countByBookIdAndEstado(
                2L,
                EstadoCopia.AVAILABLE
        )).thenReturn(3L);

        Long resultado =
                copiaService.contarCopiasDisponiblesPorLibro(2L);

        assertNotNull(resultado);
        assertEquals(3L, resultado);

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .countByBookIdAndEstado(
                        2L,
                        EstadoCopia.AVAILABLE
                );
    }

    @Test
    void actualizarCopiaCorrectamente() {
        Copia copiaExistente = Copia.builder()
                .id(2L)
                .codigoCopia("COPY-002")
                .bookId(2L)
                .estado(EstadoCopia.AVAILABLE)
                .ubicacion("Estante A-1")
                .observacion("Ubicación anterior")
                .fechaRegistro(
                        LocalDateTime.of(
                                2026,
                                6,
                                29,
                                3,
                                20
                        )
                )
                .build();

        CopiaRequestDTO request = new CopiaRequestDTO(
                "COPY-002",
                2L,
                null,
                "Estante B-4",
                "Ubicación actualizada"
        );

        BookResponseDTO libro = new BookResponseDTO(
                2L,
                "Historia de Chile",
                "Francisco Antonio Encina",
                "Literatura",
                1
        );

        when(copiaRepository.findById(2L))
                .thenReturn(Optional.of(copiaExistente));

        when(bookClient.buscarLibroPorId(2L))
                .thenReturn(libro);

        when(copiaRepository.existsByCodigoCopiaAndIdNot(
                "COPY-002",
                2L
        )).thenReturn(false);

        when(copiaRepository.save(any(Copia.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CopiaResponseDTO resultado =
                copiaService.actualizarCopia(2L, request);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals(
                "COPY-002",
                resultado.getCodigoCopia()
        );
        assertEquals(
                2L,
                resultado.getBookId()
        );
        assertEquals(
                EstadoCopia.AVAILABLE,
                resultado.getEstado()
        );
        assertEquals(
                "Estante B-4",
                resultado.getUbicacion()
        );
        assertEquals(
                "Ubicación actualizada",
                resultado.getObservacion()
        );
        assertEquals(
                "Historia de Chile",
                resultado.getBookTitle()
        );

        verify(copiaRepository)
                .findById(2L);

        verify(bookClient)
                .buscarLibroPorId(2L);

        verify(copiaRepository)
                .existsByCodigoCopiaAndIdNot(
                        "COPY-002",
                        2L
                );

        verify(copiaRepository)
                .save(any(Copia.class));
    }
    @Test
void actualizarCopiaIntentandoCambiarEstadoLanzaBusinessException() {
    Copia copiaExistente = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-1")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            4,
                            0
                    )
            )
            .build();

    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-002",
            2L,
            EstadoCopia.LOANED,
            "Estante A-1",
            "Intento de cambiar estado mediante PUT"
    );

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copiaExistente));

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.actualizarCopia(2L, request)
    );

    assertTrue(
            exception.getMessage()
                    .contains("PATCH /copias/{id}/estado")
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .save(any(Copia.class));

    verifyNoInteractions(bookClient);
}

@Test
void actualizarCopiaConCodigoDuplicadoLanzaBusinessException() {
    Copia copiaExistente = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-1")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            4,
                            10
                    )
            )
            .build();

    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-999",
            2L,
            null,
            "Estante B-1",
            "Código perteneciente a otra copia"
    );

    BookResponseDTO libro = new BookResponseDTO(
            2L,
            "Historia de Chile",
            "Francisco Antonio Encina",
            "Literatura",
            1
    );

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copiaExistente));

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    when(copiaRepository.existsByCodigoCopiaAndIdNot(
            "COPY-999",
            2L
    )).thenReturn(true);

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.actualizarCopia(2L, request)
    );

    assertTrue(
            exception.getMessage().contains("COPY-999")
    );

    verify(copiaRepository)
            .findById(2L);

    verify(bookClient)
            .buscarLibroPorId(2L);

    verify(copiaRepository)
            .existsByCodigoCopiaAndIdNot(
                    "COPY-999",
                    2L
            );

    verify(copiaRepository, never())
            .save(any(Copia.class));
    }
    @Test
void cambiarEstadoDeAvailableALoanedCorrectamente() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-3")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            4,
                            20
                    )
            )
            .build();

    BookResponseDTO libro = new BookResponseDTO(
            2L,
            "Historia de Chile",
            "Francisco Antonio Encina",
            "Literatura",
            1
    );

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    when(copiaRepository.save(any(Copia.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    CopiaResponseDTO resultado =
            copiaService.cambiarEstado(
                    2L,
                    EstadoCopia.LOANED
            );

    assertNotNull(resultado);
    assertEquals(
            EstadoCopia.LOANED,
            resultado.getEstado()
    );
    assertEquals(
            "COPY-002",
            resultado.getCodigoCopia()
    );
    assertEquals(
            "Historia de Chile",
            resultado.getBookTitle()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository)
            .save(any(Copia.class));

    verify(bookClient)
            .buscarLibroPorId(2L);
}

@Test
void cambiarEstadoAlMismoEstadoNoGuardaLaCopia() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-3")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            4,
                            30
                    )
            )
            .build();

    BookResponseDTO libro = new BookResponseDTO(
            2L,
            "Historia de Chile",
            "Francisco Antonio Encina",
            "Literatura",
            1
    );

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    CopiaResponseDTO resultado =
            copiaService.cambiarEstado(
                    2L,
                    EstadoCopia.AVAILABLE
            );

    assertNotNull(resultado);
    assertEquals(
            EstadoCopia.AVAILABLE,
            resultado.getEstado()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .save(any(Copia.class));

    verify(bookClient)
            .buscarLibroPorId(2L);
}
@Test
void cambiarEstadoDeDamagedALoanedLanzaBusinessException() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.DAMAGED)
            .ubicacion("Estante de reparación")
            .observacion("Copia dañada")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            4,
                            40
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.cambiarEstado(
                    2L,
                    EstadoCopia.LOANED
            )
    );

    assertEquals(
            "No se permite cambiar una copia de DAMAGED a LOANED",
            exception.getMessage()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .save(any(Copia.class));

    verifyNoInteractions(bookClient);
}

@Test
void cambiarEstadoConNuevoEstadoNuloLanzaBusinessException() {
    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.cambiarEstado(2L, null)
    );

    assertEquals(
            "El nuevo estado de la copia es obligatorio",
            exception.getMessage()
    );

    verifyNoInteractions(
            copiaRepository,
            bookClient
    );
}
@Test
void eliminarCopiaDisponibleCorrectamente() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-3")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            5,
                            0
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    copiaService.eliminarCopia(2L);

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository)
            .delete(copia);

    verifyNoInteractions(bookClient);
}

@Test
void eliminarCopiaPrestadaLanzaBusinessException() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.LOANED)
            .ubicacion("Estante A-3")
            .observacion("Copia prestada")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            5,
                            10
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.eliminarCopia(2L)
    );

    assertEquals(
            "No se puede eliminar una copia que está prestada",
            exception.getMessage()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .delete(any(Copia.class));

    verifyNoInteractions(bookClient);
}
@Test
void eliminarCopiaReservadaLanzaBusinessException() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.RESERVED)
            .ubicacion("Estante A-3")
            .observacion("Copia reservada")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            5,
                            20
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.eliminarCopia(2L)
    );

    assertEquals(
            "No se puede eliminar una copia que está reservada",
            exception.getMessage()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .delete(any(Copia.class));

    verifyNoInteractions(bookClient);
}

@Test
void listarCopiasPorEstadoNuloLanzaBusinessException() {
    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.listarCopiasPorEstado(null)
    );

    assertEquals(
            "El estado de la copia es obligatorio",
            exception.getMessage()
    );

    verifyNoInteractions(
            copiaRepository,
            bookClient
    );
}
@Test
void crearCopiaCuandoBookServiceDevuelveNuloLanzaRemoteServiceException() {
    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-020",
            2L,
            null,
            "Estante C-1",
            "Prueba respuesta nula"
    );

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(null);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> copiaService.crearCopia(request)
    );

    assertTrue(
            exception.getMessage().contains("respuesta vacía")
    );

    verify(bookClient)
            .buscarLibroPorId(2L);

    verifyNoInteractions(copiaRepository);
}

@Test
void crearCopiaCuandoBookServiceFallaLanzaRemoteServiceException() {
    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-021",
            2L,
            null,
            "Estante C-2",
            "Prueba error remoto"
    );

    when(bookClient.buscarLibroPorId(2L))
            .thenThrow(new RuntimeException("Servicio no disponible"));

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> copiaService.crearCopia(request)
    );

    assertEquals(
            "No fue posible comunicarse con book-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(2L);

    verifyNoInteractions(copiaRepository);
}
@Test
void buscarCopiaPorIdCeroLanzaBusinessException() {
    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.buscarCopiaPorId(0L)
    );

    assertEquals(
            "El identificador de copia debe ser mayor que cero",
            exception.getMessage()
    );

    verifyNoInteractions(
            copiaRepository,
            bookClient
    );
}

@Test
void buscarCopiaPorIdCuandoBookServiceFallaDevuelveRespuestaSinDatosDelLibro() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-3")
            .observacion("Copia disponible")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            6,
                            0
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    when(bookClient.buscarLibroPorId(2L))
            .thenThrow(new RuntimeException("book-service no disponible"));

    CopiaResponseDTO resultado =
            copiaService.buscarCopiaPorId(2L);

    assertNotNull(resultado);
    assertEquals(2L, resultado.getId());
    assertEquals("COPY-002", resultado.getCodigoCopia());
    assertEquals(null, resultado.getBookTitle());
    assertEquals(null, resultado.getBookAuthor());
    assertEquals(
            EstadoCopia.AVAILABLE,
            resultado.getEstado()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(bookClient)
            .buscarLibroPorId(2L);
}
@Test
void crearCopiaCuandoBookServiceDevuelve404LanzaResourceNotFoundException() {
    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-030",
            999L,
            null,
            "Estante D-1",
            "Libro inexistente"
    );

    feign.FeignException feignException =
            org.mockito.Mockito.mock(feign.FeignException.class);

    when(feignException.status())
            .thenReturn(404);

    when(bookClient.buscarLibroPorId(999L))
            .thenThrow(feignException);

    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> copiaService.crearCopia(request)
    );

    assertEquals(
            "No existe un libro en book-service con ID: 999",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(999L);

    verifyNoInteractions(copiaRepository);
}

@Test
void crearCopiaCuandoBookServiceDevuelve500LanzaRemoteServiceException() {
    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-031",
            2L,
            null,
            "Estante D-2",
            "Error HTTP remoto"
    );

    feign.FeignException feignException =
            org.mockito.Mockito.mock(feign.FeignException.class);

    when(feignException.status())
            .thenReturn(500);

    when(bookClient.buscarLibroPorId(2L))
            .thenThrow(feignException);

    RemoteServiceException exception = assertThrows(
            RemoteServiceException.class,
            () -> copiaService.crearCopia(request)
    );

    assertEquals(
            "No fue posible comunicarse con book-service",
            exception.getMessage()
    );

    verify(bookClient)
            .buscarLibroPorId(2L);

    verifyNoInteractions(copiaRepository);
}
@Test
void cambiarEstadoCuandoEstadoActualEsNuloLanzaBusinessException() {
    Copia copia = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(null)
            .ubicacion("Estante A-3")
            .observacion("Copia sin estado")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            6,
                            10
                    )
            )
            .build();

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copia));

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> copiaService.cambiarEstado(
                    2L,
                    EstadoCopia.AVAILABLE
            )
    );

    assertEquals(
            "La copia no posee un estado actual válido",
            exception.getMessage()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(copiaRepository, never())
            .save(any(Copia.class));

    verifyNoInteractions(bookClient);
}

@Test
void actualizarCopiaManteniendoElMismoEstadoCorrectamente() {
    Copia copiaExistente = Copia.builder()
            .id(2L)
            .codigoCopia("COPY-002")
            .bookId(2L)
            .estado(EstadoCopia.AVAILABLE)
            .ubicacion("Estante A-1")
            .observacion("Ubicación anterior")
            .fechaRegistro(
                    LocalDateTime.of(
                            2026,
                            6,
                            29,
                            6,
                            20
                    )
            )
            .build();

    CopiaRequestDTO request = new CopiaRequestDTO(
            "COPY-002",
            2L,
            EstadoCopia.AVAILABLE,
            "Estante C-5",
            "Actualización manteniendo estado"
    );

    BookResponseDTO libro = new BookResponseDTO(
            2L,
            "Historia de Chile",
            "Francisco Antonio Encina",
            "Literatura",
            1
    );

    when(copiaRepository.findById(2L))
            .thenReturn(Optional.of(copiaExistente));

    when(bookClient.buscarLibroPorId(2L))
            .thenReturn(libro);

    when(copiaRepository.existsByCodigoCopiaAndIdNot(
            "COPY-002",
            2L
    )).thenReturn(false);

    when(copiaRepository.save(any(Copia.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    CopiaResponseDTO resultado =
            copiaService.actualizarCopia(2L, request);

    assertNotNull(resultado);
    assertEquals(
            EstadoCopia.AVAILABLE,
            resultado.getEstado()
    );
    assertEquals(
            "Estante C-5",
            resultado.getUbicacion()
    );
    assertEquals(
            "Actualización manteniendo estado",
            resultado.getObservacion()
    );

    verify(copiaRepository)
            .findById(2L);

    verify(bookClient)
            .buscarLibroPorId(2L);

    verify(copiaRepository)
            .existsByCodigoCopiaAndIdNot(
                    "COPY-002",
                    2L
            );

    verify(copiaRepository)
            .save(any(Copia.class));
}
}
