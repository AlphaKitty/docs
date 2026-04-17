package com.expertlink.security;

import com.expertlink.config.JwtProperties;
import com.expertlink.domain.User;
import com.expertlink.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        try {
            Claims claims = jwtService.parse(token);
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                filterChain.doFilter(request, response);
                return;
            }
            String username = claims.getSubject();
            Number uid = claims.get("uid", Number.class);
            if (username == null || uid == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            User dbUser = userRepository.findById(uid.longValue()).orElse(null);
            if (dbUser == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (Boolean.FALSE.equals(dbUser.getIsActive())) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            int currentTe = dbUser.getTokenEpoch() == null ? 0 : dbUser.getTokenEpoch();
            Number teClaim = claims.get("te", Number.class);
            int tokenTe = teClaim == null ? 0 : teClaim.intValue();
            if (tokenTe != currentTe) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            AuthPrincipal principal = new AuthPrincipal(uid.longValue(), username);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    userDetails.getAuthorities()
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
