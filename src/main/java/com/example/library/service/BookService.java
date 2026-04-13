package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.security.LibraryUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    private static final int MAX_PAGE_SIZE = 50;

    private long requireTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LibraryUserDetails)) {
            throw new IllegalStateException("未登录或会话已过期，请重新登录");
        }
        return ((LibraryUserDetails) auth.getPrincipal()).getTenantId();
    }

    /**
     * 按 id 升序分页；页码或每页条数非法时自动纠正；页码超出末页时回到最后一页。
     */
    @Transactional(readOnly = true)
    public Page<Book> listPage(int page, int size) {
        long tenantId = requireTenantId();
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        long total = bookMapper.countAll(tenantId);
        int totalPages = total == 0 ? 0 : (int) ((total + safeSize - 1) / safeSize);
        int pageToUse = safePage;
        if (totalPages > 0 && safePage >= totalPages) {
            pageToUse = totalPages - 1;
        }
        int offset = pageToUse * safeSize;
        List<Book> content = bookMapper.selectPage(tenantId, offset, safeSize);
        Pageable pageable = PageRequest.of(pageToUse, safeSize, Sort.by(Sort.Direction.ASC, "id"));
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<Book> getById(Long id) {
        return Optional.ofNullable(bookMapper.selectById(requireTenantId(), id));
    }

    @Transactional
    public Book save(Book book) {
        long tenantId = requireTenantId();
        book.setTenantId(tenantId);
        if (book.getId() == null) {
            bookMapper.insert(book);
        } else {
            bookMapper.updateById(book);
        }
        return book;
    }

    @Transactional
    public void deleteById(Long id) {
        bookMapper.deleteById(requireTenantId(), id);
    }
}
