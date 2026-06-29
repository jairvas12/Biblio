package com.library.find_service.service.impl;

import com.library.find_service.client.BookClient;
import com.library.find_service.dto.BookDTO;
import com.library.find_service.exception.BusinessException;
import com.library.find_service.service.FindService;
import com.library.find_service.exception.RemoteServiceException;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class FindServiceImpl
        implements FindService {

    private final BookClient bookClient;

    public FindServiceImpl(
            BookClient bookClient
    ) {
        this.bookClient =
                bookClient;
    }

    @Override
    public List<BookDTO> getAllBooks() {

        try {
            List<BookDTO> books =
                    bookClient.getAllBooks();

            if (books == null) {

                log.warn(
                        "BOOK devolvió una lista nula"
                );

                return List.of();
            }

            log.info(
                    "Catálogo obtenido desde BOOK. total={}",
                    books.size()
            );

            return books;

        } catch (FeignException exception) {

            int status =
                    exception.status();

            if (
                    status == 401
                            || status == 403
            ) {
                log.error(
                        "BOOK rechazó la solicitud de FIND. status={}",
                        status
                );

                throw new RemoteServiceException(
                        "BOOK rechazó la solicitud de consulta"
                );
            }

            log.error(
                    "No fue posible obtener el catálogo desde BOOK. status={}",
                    status,
                    exception
            );

            throw new RemoteServiceException(
                    "No fue posible obtener el catálogo desde BOOK"
            );
        }
    }

    @Override
    public List<BookDTO> findByTitle(
            String title
    ) {
        String normalizedTitle =
                normalizeSearchTerm(
                        title,
                        "título"
                );

        List<BookDTO> results =
                getAllBooks()
                        .stream()
                        .filter(book ->
                                containsIgnoreCase(
                                        book.getTitle(),
                                        normalizedTitle
                                )
                        )
                        .toList();

        log.info(
                "Búsqueda por título. término={}, resultados={}",
                normalizedTitle,
                results.size()
        );

        return results;
    }

    @Override
    public List<BookDTO> findByAuthor(
            String author
    ) {
        String normalizedAuthor =
                normalizeSearchTerm(
                        author,
                        "autor"
                );

        List<BookDTO> results =
                getAllBooks()
                        .stream()
                        .filter(book ->
                                containsIgnoreCase(
                                        book.getAuthor(),
                                        normalizedAuthor
                                )
                        )
                        .toList();

        log.info(
                "Búsqueda por autor. término={}, resultados={}",
                normalizedAuthor,
                results.size()
        );

        return results;
    }

    @Override
    public List<BookDTO> findByCategory(
            String category
    ) {
        String normalizedCategory =
                normalizeSearchTerm(
                        category,
                        "categoría"
                );

        List<BookDTO> results =
                getAllBooks()
                        .stream()
                        .filter(book ->
                                containsIgnoreCase(
                                        book.getCategory(),
                                        normalizedCategory
                                )
                        )
                        .toList();

        log.info(
                "Búsqueda por categoría. término={}, resultados={}",
                normalizedCategory,
                results.size()
        );

        return results;
    }

    private String normalizeSearchTerm(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new BusinessException(
                    "El parámetro "
                            + fieldName
                            + " es obligatorio"
            );
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private boolean containsIgnoreCase(
            String source,
            String normalizedTerm
    ) {
        return source != null
                && source
                .toLowerCase(
                        Locale.ROOT
                )
                .contains(
                        normalizedTerm
                );
    }
}