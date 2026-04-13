package com.example.library.mapper;

import com.example.library.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {

    Tenant selectByCode(@Param("code") String code);

    Tenant selectById(@Param("id") Long id);

    long countByCode(@Param("code") String code);

    List<Tenant> selectAll();

    int insert(Tenant tenant);

    int update(Tenant tenant);
}
