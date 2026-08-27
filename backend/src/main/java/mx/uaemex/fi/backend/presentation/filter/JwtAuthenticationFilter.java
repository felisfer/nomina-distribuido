package mx.uaemex.fi.backend.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mx.uaemex.fi.backend.logic.service.CustomUserDetailsService;
import mx.uaemex.fi.backend.logic.service.JwtService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var auth = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (Objects.isNull(auth) || !auth.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            var jwt = auth.substring(7);

            if (jwtService.validateToken(jwt)) {
                var rfc = jwtService.getRfcFromToken(jwt);

                var user = userDetailsService.loadUserByUsername(rfc);

                var authToken = new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                return;
            }
        } catch (UsernameNotFoundException e) {
            return;
        }

        filterChain.doFilter(request, response);
    }
}
