package com.example.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 将 Vue Router 路径转发到 {@code index.html}，支持直接访问或刷新子路由。
 */
@Controller
public class SpaController {

    /** 首访根路径直接进登录页，避免仅打开域名时停留在空白或未授权路由 */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping({"/login", "/books", "/books/new", "/books/edit"})
    public String spaRoutes() {
        return "forward:/index.html";
    }

    @GetMapping({"/admin", "/admin/tenants", "/admin/tenant-auth"})
    public String adminSpa() {
        return "forward:/index.html";
    }

    @GetMapping("/admin/tenants/{tenantId:\\d+}/users")
    public String adminTenantUsersSpa(@SuppressWarnings("unused") @PathVariable("tenantId") String tenantId) {
        return "forward:/index.html";
    }
}
