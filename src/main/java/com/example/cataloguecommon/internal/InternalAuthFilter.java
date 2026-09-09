package com.example.cataloguecommon.internal;

import com.example.cataloguecommon.internal.InternalAuth.InvalidInternalAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Rejects a write ({@code POST/PUT/DELETE/PATCH} under {@code /api/**}) that
 * doesn't carry a valid {@link InternalAuth#HEADER} (SECURITY.md C2). Reads stay
 * open. Disable with {@code internal-auth.enabled=false} (tests do).
 *
 * <p>A plain servlet filter — the catalogues have no Spring Security. Registered
 * automatically because it's a {@code @Component} of type {@code Filter}.
 */
@Component
public class InternalAuthFilter extends OncePerRequestFilter {

    private static final Set<String> WRITES = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final boolean enabled;
    private final String secret;

    public InternalAuthFilter(@Value("${internal-auth.enabled:true}") boolean enabled,
                              @Value("${internal-auth.secret:dev-internal-secret-change-me}") String secret) {
        this.enabled = enabled;
        this.secret = secret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = request.getRequestURI();
        return !(WRITES.contains(request.getMethod()) && path.startsWith("/api/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            InternalAuth.verify(secret, request.getHeader(InternalAuth.HEADER),
                    request.getMethod(), request.getRequestURI());
        } catch (InvalidInternalAuthException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"status\":403,\"title\":\"Forbidden\",\"detail\":\"internal auth: " + e.getMessage() + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
