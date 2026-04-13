package com.example.library.dto;

import com.example.library.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public class BookPageResponse {

    private List<BookDto> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static BookPageResponse from(Page<Book> page) {
        BookPageResponse r = new BookPageResponse();
        r.setContent(page.getContent().stream().map(BookDto::fromEntity).toList());
        r.setTotalElements(page.getTotalElements());
        r.setTotalPages(page.getTotalPages());
        r.setNumber(page.getNumber());
        r.setSize(page.getSize());
        return r;
    }

    public List<BookDto> getContent() {
        return content;
    }

    public void setContent(List<BookDto> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
