package com.example.library.mapper;

import com.example.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {

    Book selectById(@Param("tenantId") long tenantId, @Param("id") Long id);

    long countAll(@Param("tenantId") long tenantId);

    List<Book> selectPage(
            @Param("tenantId") long tenantId, @Param("offset") int offset, @Param("limit") int limit);

    int insert(Book book);

    int updateById(Book book);

    int deleteById(@Param("tenantId") long tenantId, @Param("id") Long id);
}
