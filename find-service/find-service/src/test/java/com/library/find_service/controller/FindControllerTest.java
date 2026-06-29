package com.library.find_service.controller;

import com.library.find_service.dto.BookDTO;
import com.library.find_service.service.FindService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import com.library.find_service.exception.BusinessException;
import com.library.find_service.exception.GlobalExceptionHandler;
import com.library.find_service.exception.RemoteServiceException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FindControllerTest {

    @Mock
    private FindService findService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        FindController controller =
                new FindController(
                        findService
                );

    mockMvc =
            MockMvcBuilders
                    .standaloneSetup(
                            controller
                    )
                    .setControllerAdvice(
                            new GlobalExceptionHandler()
                    )
                    .build();
    }

    @Test
    void getAllBooksShouldReturnStatusOkAndCatalog()
            throws Exception {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "Historia de Chile",
                                "Francisco Encina",
                                "Historia"
                        ),
                        createBook(
                                2L,
                                "La casa de los espíritus",
                                "Isabel Allende",
                                "Novela"
                        )
                );

        when(
                findService.getAllBooks()
        ).thenReturn(
                books
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$.length()"
                        ).value(2)
                )
                .andExpect(
                        jsonPath(
                                "$[0].title"
                        ).value(
                                "Historia de Chile"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[1].author"
                        ).value(
                                "Isabel Allende"
                        )
                );

        verify(
                findService
        ).getAllBooks();
    }

    @Test
    void findByTitleShouldReturnStatusOkAndResults()
            throws Exception {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                1L,
                                "Historia de Chile",
                                "Francisco Encina",
                                "Historia"
                        )
                );

        when(
                findService.findByTitle(
                        "Historia"
                )
        ).thenReturn(
                books
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/title"
                        )
                                .param(
                                        "title",
                                        "Historia"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$[0].title"
                        ).value(
                                "Historia de Chile"
                        )
                );

        verify(
                findService
        ).findByTitle(
                "Historia"
        );
    }

    @Test
    void findByAuthorShouldReturnStatusOkAndResults()
            throws Exception {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                2L,
                                "La casa de los espíritus",
                                "Isabel Allende",
                                "Novela"
                        )
                );

        when(
                findService.findByAuthor(
                        "Allende"
                )
        ).thenReturn(
                books
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/author"
                        )
                                .param(
                                        "author",
                                        "Allende"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$[0].author"
                        ).value(
                                "Isabel Allende"
                        )
                );

        verify(
                findService
        ).findByAuthor(
                "Allende"
        );
    }

    @Test
    void findByCategoryShouldReturnStatusOkAndResults()
            throws Exception {

        // Given
        List<BookDTO> books =
                List.of(
                        createBook(
                                2L,
                                "La casa de los espíritus",
                                "Isabel Allende",
                                "Novela"
                        )
                );

        when(
                findService.findByCategory(
                        "Novela"
                )
        ).thenReturn(
                books
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/category"
                        )
                                .param(
                                        "category",
                                        "Novela"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath(
                                "$[0].category"
                        ).value(
                                "Novela"
                        )
                );

        verify(
                findService
        ).findByCategory(
                "Novela"
        );
    }

    @Test
    void findByTitleShouldReturnBadRequestWhenServiceThrowsBusinessException()
            throws Exception {

        // Given
        when(
                findService.findByTitle(
                        ""
                )
        ).thenThrow(
                new BusinessException(
                        "El parámetro título es obligatorio"
                )
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/title"
                        )
                                .param(
                                        "title",
                                        ""
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(400)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "BUSINESS ERROR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "El parámetro título es obligatorio"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                "/find/title"
                        )
                );

        verify(
                findService
        ).findByTitle(
                ""
        );
    }

    @Test
    void getAllBooksShouldReturnServiceUnavailableWhenBookServiceFails()
            throws Exception {

        // Given
        when(
                findService.getAllBooks()
        ).thenThrow(
                new RemoteServiceException(
                        "No fue posible obtener el catálogo desde BOOK"
                )
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                )
                .andExpect(
                        status().isServiceUnavailable()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(503)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "REMOTE SERVICE ERROR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "No fue posible obtener el catálogo desde BOOK"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                "/find/books"
                        )
                );

        verify(
                findService
        ).getAllBooks();
    }

    @Test
    void getAllBooksShouldReturnInternalServerErrorWhenUnexpectedErrorOccurs()
            throws Exception {

        // Given
        when(
                findService.getAllBooks()
        ).thenThrow(
                new RuntimeException(
                        "Error inesperado"
                )
        );

        // When - Then
        mockMvc.perform(
                        get(
                                "/find/books"
                        )
                )
                .andExpect(
                        status().isInternalServerError()
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(500)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value(
                                "INTERNAL SERVER ERROR"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Ocurrió un error interno en el servidor"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                "/find/books"
                        )
                );

        verify(
                findService
        ).getAllBooks();
    }

    private BookDTO createBook(
            Long id,
            String title,
            String author,
            String category
    ) {
        return BookDTO.builder()
                .id(id)
                .isbn(
                        "9789560000000"
                )
                .title(title)
                .author(author)
                .publicationYear(
                        2020
                )
                .categoryId(
                        1L
                )
                .category(category)
                .build();
    }
}