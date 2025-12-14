package com.greenride.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Βοηθητική κλάση (Utility Component) για την ανάκτηση του τρέχοντος συνδεδεμένου χρήστη.
 * Αφαιρεί την πολυπλοκότητα του Spring Security context από τα Services και Controllers.
 */
@Component
public class CurrentUserProvider {

    /**
     * Επιστρέφει τον τρέχοντα χρήστη αν είναι συνδεδεμένος.
     *
     * @return Ένα Optional που περιέχει το αντικείμενο CurrentUser ή είναι κενό αν δεν υπάρχει χρήστης.
     */
    public Optional<CurrentUser> getCurrentUser() {
        
        // 1. Ανάκτηση του αντικειμένου Authentication από το SecurityContextHolder.
        // Το SecurityContextHolder είναι το σημείο όπου το Spring αποθηκεύει ποιος είναι logged in στο τρέχον thread.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Έλεγχος αν υπάρχει authentication και αν ο χρήστης είναι πραγματικά συνδεδεμένος.
        // Το isAuthenticated() επιστρέφει false αν είναι π.χ. ανώνυμος χρήστης.
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // 3. Ανάκτηση του "Principal" (η ταυτότητα του χρήστη).
        // Στην περίπτωσή μας, επειδή χρησιμοποιούμε τον CustomUserDetails, το principal θα είναι αυτού του τύπου.
        Object principal = authentication.getPrincipal();

        // 4. Έλεγχος τύπου και μετατροπή (Cast).
        // Αν το principal είναι τύπου CustomUserDetails, δημιουργούμε το DTO CurrentUser.
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(new CurrentUser(
                    userDetails.getId(),       // ID χρήστη από τη βάση
                    userDetails.getUsername(), // Username
                    userDetails.getEmail()     // Email
            ));
        }

        // Αν δεν βρεθεί κατάλληλος χρήστης (π.χ. είναι "anonymousUser"), επιστρέφουμε κενό.
        return Optional.empty();
    }
}
