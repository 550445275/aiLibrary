package com.example.library.service.platform;

import com.example.library.dto.platform.TenantCreateRequest;
import com.example.library.dto.platform.TenantDto;
import com.example.library.dto.platform.TenantUpdateRequest;
import com.example.library.entity.Tenant;
import com.example.library.mapper.TenantMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PlatformTenantService {

    /** 小写字母开头，后续为小写字母、数字、连字符或下划线（与前端提示一致） */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{0,62}$");

    private final TenantMapper tenantMapper;

    public PlatformTenantService(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Transactional(readOnly = true)
    public List<TenantDto> listAll() {
        return tenantMapper.selectAll().stream().map(TenantDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TenantDto getById(Long id) {
        Tenant t = tenantMapper.selectById(id);
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "租户不存在");
        }
        return TenantDto.fromEntity(t);
    }

    @Transactional
    public TenantDto create(TenantCreateRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()
                || req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code 与 name 不能为空");
        }
        String code = normalizeTenantCode(req.getCode());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "租户代码须为小写字母开头，仅含小写字母、数字、连字符或下划线，长度不超过 63");
        }
        if (tenantMapper.countByCode(code) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "租户代码已存在");
        }
        Tenant t = new Tenant();
        t.setCode(code);
        t.setName(req.getName().trim());
        t.setEnabled(req.getEnabled() == null || req.getEnabled());
        tenantMapper.insert(t);
        return TenantDto.fromEntity(tenantMapper.selectById(t.getId()));
    }

    @Transactional
    public TenantDto update(Long id, TenantUpdateRequest req) {
        Tenant existing = tenantMapper.selectById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "租户不存在");
        }
        if (req.getName() != null && !req.getName().isBlank()) {
            existing.setName(req.getName().trim());
        }
        if (req.getEnabled() != null) {
            existing.setEnabled(req.getEnabled());
        }
        tenantMapper.update(existing);
        return TenantDto.fromEntity(tenantMapper.selectById(id));
    }

    private static String normalizeTenantCode(String raw) {
        if (raw == null) {
            return "";
        }
        String n = Normalizer.normalize(raw.trim(), Normalizer.Form.NFKC).toLowerCase();
        return n;
    }
}
