package com.greenride.controller.web;

import com.greenride.dto.RegisterRequest;
import com.greenride.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller υπεύθυνος για την εγγραφή χρηστών μέσω της διεπαφής Web.
 */
@Controller
public class WebRegistrationController {

    private final UserService userService;

    // Injection του UserService για να εκτελέσουμε την επιχειρησιακή λογική εγγραφής
    @Autowired
    public WebRegistrationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Εμφανίζει τη φόρμα εγγραφής.
     * Αν ο χρήστης είναι ήδη συνδεδεμένος, τον στέλνει στην αρχική.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Authentication authentication, Model model) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/";
        }
        // Προσθέτουμε ένα κενό αντικείμενο RegisterRequest στο μοντέλο για τη φόρμα
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", ""));
        return "register"; // Επιστρέφει το register.html
    }

    /**
     * Επεξεργάζεται την υποβολή της φόρμας εγγραφής (POST request).
     *
     * @param registerRequest Τα δεδομένα που συμπλήρωσε ο χρήστης.
     * @param bindingResult Περιέχει τυχόν σφάλματα επικύρωσης (validation errors).
     * @param model Το μοντέλο για να στείλουμε δεδομένα πίσω στο view.
     * @return Το επόμενο view ή ανακατεύθυνση.
     */
    @PostMapping("/register")
    public String handleRegistration(
            Authentication authentication,
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest, // Το @Valid ενεργοποιεί τους ελέγχους (π.χ. size, email)
            BindingResult bindingResult, // Εδώ αποθηκεύονται τα αποτελέσματα του ελέγχου
            Model model) {

        // Αν είναι ήδη συνδεδεμένος, δεν χρειάζεται να εγγραφεί
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        // Αν υπάρχουν σφάλματα στη φόρμα (π.χ. κενό username), επιστρέφουμε στη φόρμα
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Καλούμε το service για να δημιουργήσουμε τον χρήστη στη βάση
            userService.registerUser(registerRequest);
            // Ανακατεύθυνση στο login με μήνυμα επιτυχίας
            return "redirect:/login?success";
        } catch (Exception e) {
            // Σε περίπτωση λάθους (π.χ. το username υπάρχει ήδη), το εμφανίζουμε στη σελίδα
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerRequest", registerRequest); // Κρατάμε τα δεδομένα για να μην τα ξαναγράφει ο χρήστης
            return "register";
        }
    }
}
