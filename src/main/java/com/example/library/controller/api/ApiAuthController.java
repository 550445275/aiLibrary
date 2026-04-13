package com.example.library.controller.api;

import com.example.library.dto.LoginRequest;
import com.example.library.dto.UserInfoResponse;
import com.example.library.security.LibraryUserDetails;
import com.example.library.security.TenantLoginContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public ApiAuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    private static SecurityContextHolderStrategy contextHolderStrategy() {
        return SecurityContextHolder.getContextHolderStrategy();
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfoResponse> login(
            @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (body.getTenantCode() == null || body.getTenantCode().isBlank()
                || body.getUsername() == null || body.getUsername().isBlank()
                || body.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        TenantLoginContext.setTenantCode(body.getTenantCode().trim());
        try {
            Authentication token = UsernamePasswordAuthenticationToken.unauthenticated(
                    body.getUsername().trim(), body.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContext context = contextHolderStrategy().createEmptyContext();
            context.setAuthentication(authentication);
            contextHolderStrategy().setContext(context);
            securityContextRepository.saveContext(context, request, response);
            if (authentication.getPrincipal() instanceof LibraryUserDetails lud) {
                return ResponseEntity.ok(UserInfoResponse.fromLibraryUser(lud));
            }
            return ResponseEntity.ok(new UserInfoResponse());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } finally {
            TenantLoginContext.clear();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = contextHolderStrategy().getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me() {
        Authentication authentication = contextHolderStrategy().getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (authentication.getPrincipal() instanceof LibraryUserDetails lud) {
            return ResponseEntity.ok(UserInfoResponse.fromLibraryUser(lud));
        }
        UserInfoResponse fallback = new UserInfoResponse();
        fallback.setUsername(authentication.getName());
        return ResponseEntity.ok(fallback);
    }
}
