package com.library.book_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_books_isbn",
                        columnNames = "isbn"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 20
    )
    private String isbn;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            nullable = false,
            length = 150
    )
    private String author;

    @Column(
            name = "publication_year",
            nullable = false
    )
    private Integer publicationYear;

    @Column(
            name = "category_id",
            nullable = false
    )
    private Long categoryId;
}