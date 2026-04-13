package com.example.library.mapper;

import com.example.library.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppUserMapper {

    long countUsersByTenant(@Param("tenantId") long tenantId);

    AppUser selectByTenantAndUsername(
            @Param("tenantId") long tenantId, @Param("username") String username);

    AppUser selectById(@Param("id") Long id);

    List<AppUser> listByTenantId(@Param("tenantId") long tenantId);

    int insert(AppUser user);

    int updateForPlatform(AppUser user);
}
