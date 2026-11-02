package com.h_group.gvs_terceirizacoes.config;

import com.h_group.gvs_terceirizacoes.dto.response.ErrorResponse;
import com.h_group.gvs_terceirizacoes.entity.User;
import com.h_group.gvs_terceirizacoes.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PROTECTED_PREFIXES = List.of(
            "/api/clientes",
            "/api/prestadores",
            "/api/contratos",
            "/api/ordens-servico",
            "/api/historico",
            "/api/auth/me"
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        return PROTECTED_PREFIXES.stream()
                .noneMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "Authentication token not provided");
            return;
        }

        String token = authorization.substring(7).trim();

        UUID userId;
        try {
            userId = UUID.fromString(jwtService.parseToken(token).getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }
        String path = UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        // Use the current database role so a permission change also affects already-issued tokens.
        if (!AccessPolicy.permits(user.getRole(), request.getMethod(), path)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Your profile is not allowed to perform this operation");
            return;
        }
        request.setAttribute("authenticatedUserId", userId);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new ErrorResponse("error", message));
    }
}
