package com.greenride.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller για τη διαχείριση της αρχικής σελίδας και της αποσύνδεσης.
 * Χρησιμοποιεί το Thymeleaf για την παραγωγή HTML.
 */
@Controller
public class WebHomeController {

    /**
     * Διαχειρίζεται το root path ("/").
     * Αν ο χρήστης είναι ήδη συνδεδεμένος, τον ανακατευθύνει αυτόματα
     * στη σελίδα αναζήτησης διαδρομών (/rides).
     * Διαφορετικά, εμφανίζει την αρχική σελίδα (index.html).
     *
     * @param authentication Το αντικείμενο πιστοποίησης του Spring Security.
     * @return Το όνομα του template ή η εντολή ανακατεύθυνσης.
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        // Έλεγχος αν ο χρήστης είναι αυθεντικοποιημένος
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/rides"; // Ανακατεύθυνση στην εφαρμογή αν είναι ήδη logged in
        }
        return "index"; // Επιστροφή του index.html (Landing Page)
    }

    /**
     * Εμφανίζει τη σελίδα επιβεβαίωσης αποσύνδεσης.
     * Αυτό είναι ένα ξεχωριστό view για να μην γίνεται logout κατά λάθος.
     *
     * @return Το όνομα του template για logout (logout.html).
     */
    @GetMapping("/logout-confirm")
    public String logoutConfirm() {
        return "logout";
    }
}
