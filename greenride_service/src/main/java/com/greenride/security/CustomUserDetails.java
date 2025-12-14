package com.greenride.security;

import com.greenride.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Προσαρμοσμένη υλοποίηση του UserDetails.
 * Το Spring Security δεν γνωρίζει την κλάση "User" που έχουμε φτιάξει.
 * Γνωρίζει μόνο το interface "UserDetails".
 * Αυτή η κλάση γεφυρώνει το χάσμα, κρατώντας τα δεδομένα του χρήστη μας
 * σε μορφή που το Spring Security μπορεί να χρησιμοποιήσει για authentication & authorization.
 */
public class CustomUserDetails implements UserDetails {

    // Πεδία που χρειαζόμαστε από το δικό μας Entity
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    
    // Η λίστα με τα δικαιώματα (Roles) όπως τα καταλαβαίνει το Spring Security
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Κατασκευαστής (Constructor).
     * Παίρνει το δικό μας Entity "User" και αντιγράφει τα δεδομένα στα πεδία του UserDetails.
     * * @param user Ο χρήστης από τη βάση δεδομένων.
     */
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        
        // Μετατροπή των ρόλων (π.χ. "ROLE_ADMIN") σε αντικείμενα GrantedAuthority
        // Το Spring Security χρησιμοποιεί το SimpleGrantedAuthority για να ελέγχει την πρόσβαση (π.χ. @PreAuthorize)
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    // --- Custom Getters (Πέρα από το UserDetails) ---
    // Αυτά μας επιτρέπουν να έχουμε πρόσβαση στο ID και το Email του χρήστη
    // όταν ανακτούμε το Principal από το SecurityContext.
    
    public Long getId() { return id; }
    public String getEmail() { return email; }

    // --- Υλοποίηση μεθόδων του UserDetails ---

    /**
     * Επιστρέφει τα δικαιώματα (ρόλους) του χρήστη.
     */
    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    /**
     * Επιστρέφει τον κωδικό πρόσβασης (που είναι κρυπτογραφημένος).
     * Το Spring Security τον χρησιμοποιεί για να τον συγκρίνει με αυτόν που έδωσε ο χρήστης.
     */
    @Override 
    public String getPassword() { return password; }

    /**
     * Επιστρέφει το όνομα χρήστη.
     */
    @Override 
    public String getUsername() { return username; }

    // --- Λογική για το status του λογαριασμού ---
    // Εδώ επιστρέφουμε 'true' hardcoded για απλότητα, αλλά σε μια πιο σύνθετη εφαρμογή
    // θα μπορούσαμε να ελέγχουμε αν ο λογαριασμός έχει λήξει ή κλειδωθεί.

    @Override 
    public boolean isAccountNonExpired() { return true; } // Ο λογαριασμός δεν λήγει ποτέ

    @Override 
    public boolean isAccountNonLocked() { return true; } // Ο λογαριασμός δεν κλειδώνεται ποτέ

    @Override 
    public boolean isCredentialsNonExpired() { return true; } // Τα διαπιστευτήρια δεν λήγουν

    /**
     * Ελέγχει αν ο χρήστης είναι ενεργός.
     * Αυτό το παίρνουμε από τη βάση (πεδίο 'enabled' στον User).
     * Αν είναι false, το Spring Security θα απαγορεύσει τη σύνδεση.
     */
    @Override 
    public boolean isEnabled() { return enabled; }
}
