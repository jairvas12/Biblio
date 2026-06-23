package com.library.copia_service.repository;

import com.library.copia_service.model.Copia;
import com.library.copia_service.model.EstadoCopia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CopiaRepository extends JpaRepository<Copia, Long> {

    boolean existsByCodigoCopia(String codigoCopia);

    boolean existsByCodigoCopiaAndIdNot(String codigoCopia, Long id);

    List<Copia> findByBookId(Long bookId);

    List<Copia> findByEstado(EstadoCopia estado);

    List<Copia> findByBookIdAndEstado(Long bookId, EstadoCopia estado);

    long countByBookIdAndEstado(Long bookId, EstadoCopia estado);
}
