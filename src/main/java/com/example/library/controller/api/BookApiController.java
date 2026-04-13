package com.example.library.controller.api;

import com.example.library.dto.BookDto;
import com.example.library.dto.BookPageResponse;
import com.example.library.dto.BookSaveRequest;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public BookPageResponse list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Book> bookPage = bookService.listPage(page, size);
        return BookPageResponse.from(bookPage);
    }

    @GetMapping("/{id}")
    public BookDto getOne(@PathVariable("id") Long id) {
        return bookService.getById(id)
                .map(BookDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在"));
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@Valid @RequestBody BookSaveRequest body) {
        Book book = toEntity(body, null);
        Book saved = bookService.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookDto.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable("id") Long id, @Valid @RequestBody BookSaveRequest body) {
        bookService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在"));
        Book book = toEntity(body, id);
        return BookDto.fromEntity(bookService.save(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (bookService.getById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private static Book toEntity(BookSaveRequest body, Long id) {
        Book book = new Book();
        if (id != null) {
            book.setId(id);
        }
        book.setTitle(body.getTitle().trim());
        book.setAuthor(blankToNull(body.getAuthor()));
        book.setIsbn(blankToNull(body.getIsbn()));
        book.setPublishYear(body.getPublishYear());
        book.setPrice(body.getPrice());
        return book;
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
