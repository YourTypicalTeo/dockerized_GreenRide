package com.greenride.security;

import com.greenride.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
* Φίλτρο που παρεμβάλλεται σε κάθε αίτημα (request) για να ελέγξει τα όρια χρήσης.
 * Τρέχει ΠΡΙΝ από την αυθεντικοποίηση (Login) για να προστατεύσει τον server απο σπαμ.
 */

@Component
@Order(1) // Κρίσιμο: Πρέπει να τρέχει πρώτο-πρώτο στην αλυσίδα ασφαλείας.
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Αναγνώριση του πελάτη βάσει IP διεύθυνσης
        String clientIp = request.getRemoteAddr();

        // 2. Ανάκτηση του "κουβά" (bucket) που αντιστοιχεί σε αυτή την IP καθε ΙP έχει δικό τησ μετρητή
        Bucket bucket = rateLimitingService.resolveBucket(clientIp);

        // 3.Προσπάθεια κατανάλωσης 1 token για το αίτημα
        if (bucket.tryConsume(1)) {
            // Επιτυχία: Υπάρχουν διαθέσιμα tokens, το αίτημα προχωράει
            filterChain.doFilter(request, response);
        } else {
            // Αποτυχία: Ο χρήστης ξεπέρασε το όριο (π.χ. 50 requests/min)
            // Επιστρέφουμε HTTP 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests - Please try again later.\"}");
        }
    }
}
