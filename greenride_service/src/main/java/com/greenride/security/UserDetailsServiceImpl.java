package com.greenride.security;

import com.greenride.model.User;
import com.greenride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Υπηρεσία (Service) που υλοποιεί το interface UserDetailsService του Spring Security.
 * Ο ρόλος της είναι να φορτώνει τα δεδομένα του χρήστη από τη βάση δεδομένων
 * όταν κάποιος προσπαθεί να συνδεθεί (Login).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Αυτή η μέθοδος καλείται αυτόματα από το Spring Security κατά τη διαδικασία πιστοποίησης.
     *
     * @param username Το όνομα χρήστη που έδωσε ο χρήστης στη φόρμα σύνδεσης.
     * @return Ένα αντικείμενο UserDetails που περιέχει τα στοιχεία και τους ρόλους του χρήστη.
     * @throws UsernameNotFoundException Εξαίρεση αν ο χρήστης δεν βρεθεί στη βάση.
     */
    @Override
    @Transactional // Εξασφαλίζει ότι η συναλλαγή παραμένει ανοιχτή (σημαντικό για την ανάκτηση των ρόλων αν είναι Lazy)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. Αναζήτηση του χρήστη στη βάση δεδομένων μέσω του UserRepository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // 2. Μετατροπή του Entity User σε CustomUserDetails.
        // Το CustomUserDetails λειτουργεί ως προσαρμογέας (Adapter) ώστε το Spring Security
        // να καταλάβει τη δομή του δικού μας χρήστη.
        return new CustomUserDetails(user);
    }
}
