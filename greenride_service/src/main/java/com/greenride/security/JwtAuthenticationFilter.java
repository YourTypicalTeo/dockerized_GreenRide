package com.greenride.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Φίλτρο που εκτελείται ΜΙΑ φορά για κάθε αίτημα (OncePerRequestFilter).
 * Σκοπός: Να βρει το JWT Token, να το ελέγξει και να συνδέσει τον χρήστη στο σύστημα.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Ψάχνουμε το Header "Authorization"
        final String authHeader = request.getHeader("Authorization");

        /* 2. Αν δεν υπάρχει ή δεν ξεκινάει με "Bearer", δεν κάνουμε τίποτα*
         Αφήνουμε το αίτημα να περάσει ως "Ανώνυμο" (το SecurityConfig θα το κόψει μετά αν χρειάζεται).*/
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Απομόνωση του καθαρού Token (χωρίς το "Bearer" prefix)
        final String token = authHeader.substring(7);

        try {
            // 4. Επαλήθευση Token μέσω του JwtService (ελέγχει υπογραφή, λήξη, issuer)
            final Claims claims = jwtService.parse(token);
            final String username = claims.getSubject();

            // 5. --- STATELESS AUTHENTICATION ---
            // Αντί να ψάξουμε στη βάση (DB hit), παίρνουμε τους ρόλους ΜΕΣΑ από το Token.
            @SuppressWarnings("unchecked")
            final Collection<String> roles = (Collection<String>) claims.get("roles");

            // Μετατροπή των ρόλων σε μορφή που καταλαβαίνει το Spring Security
            final List<GrantedAuthority> authorities = roles == null
                    ? List.of()
                    : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            // 6. Δημιουργία αντικειμένου χρήστη (Principal) στη μνήμη
            final User principal = new User(username, "", authorities);
            
            // 7. Ενημέρωση του Spring Security ότι ο χρήστης είναι πλέον Authenticated
            final UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Αν το token είναι ληγμένο ή πλαστό, απλά καταγράφουμε το log.
            // Δεν σταματάμε τη ροή βίαια, αλλά ο χρήστης παραμένει ανώνυμος,
            // οπότε το επόμενο φίλτρο θα τον κόψει (401 Unauthorized).
            LOGGER.warn("Invalid JWT: {}", e.getMessage());
        }

        // Συνέχεια στο επόμενο φίλτρο της αλυσίδας
        filterChain.doFilter(request, response);
    }
}
