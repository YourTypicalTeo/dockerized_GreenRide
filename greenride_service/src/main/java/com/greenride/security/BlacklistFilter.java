package com.greenride.security;

import com.greenride.service.BlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0) // Τρέχει ΠΡΙΝ από το RateLimitFilter (που έχει Order(1))
public class BlacklistFilter extends OncePerRequestFilter {

    private final BlacklistService blacklistService;

    public BlacklistFilter(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        // Έλεγχος αν η IP είναι στη λίστα
        if (blacklistService.isBlocked(clientIp)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access Denied: Your IP has been blocked.");
            return; // Διακοπή του αιτήματος εδώ
        }

        filterChain.doFilter(request, response);
    }
}
