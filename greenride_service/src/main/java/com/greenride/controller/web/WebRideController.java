package com.greenride.controller.web;

import com.greenride.dto.CreateRideDTO;
import com.greenride.dto.RideView;
import com.greenride.model.Ride;
import com.greenride.security.CurrentUserProvider;
import com.greenride.service.BookingService;
import com.greenride.service.RideService;
import com.greenride.service.mapper.RideMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller για τη διαχείριση των λειτουργιών Ride (Διαδρομές) μέσω Web UI.
 * Περιλαμβάνει αναζήτηση, δημιουργία, προβολή και κράτηση.
 */
@Controller
public class WebRideController {

    private final RideService rideService;
    private final BookingService bookingService;
    private final RideMapper rideMapper;
    private final CurrentUserProvider currentUserProvider;

    // Constructor Injection για όλα τα απαραίτητα Services
    @Autowired
    public WebRideController(RideService rideService,
                             BookingService bookingService,
                             RideMapper rideMapper,
                             CurrentUserProvider currentUserProvider) {
        this.rideService = rideService;
        this.bookingService = bookingService;
        this.rideMapper = rideMapper;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Εμφανίζει τη σελίδα αναζήτησης (Dashboard).
     * Αν δεν δοθούν παράμετροι, εμφανίζει όλες τις διαδρομές.
     *
     * @param start (Optional) Φίλτρο για την τοποθεσία αφετηρίας.
     * @param dest (Optional) Φίλτρο για την τοποθεσία προορισμού.
     */
    @GetMapping("/rides")
    public String showRides(
            @RequestParam(required = false, defaultValue = "") String start,
            @RequestParam(required = false, defaultValue = "") String dest,
            Model model) {

        // Αναζήτηση διαδρομών μέσω του Service
        List<Ride> rides = rideService.searchRides(start, dest);

        // Μετατροπή των Entities (Ride) σε DTOs (RideView) για ασφαλή προβολή στο View
        List<RideView> rideViews = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());

        // Προσθήκη δεδομένων στο μοντέλο για χρήση από το Thymeleaf
        model.addAttribute("rides", rideViews);
        model.addAttribute("paramStart", start); // Κρατάμε την τιμή αναζήτησης στο πεδίο
        model.addAttribute("paramDest", dest);

        return "rides"; // Επιστροφή του rides.html
    }

    /**
     * Εμφανίζει τη φόρμα δημιουργίας νέας διαδρομής.
     */
    @GetMapping("/rides/create")
    public String showCreateRideForm(Model model) {
        // Προ-συμπλήρωση της φόρμας με default τιμές (π.χ. ημερομηνία +1 ώρα, 3 θέσεις)
        CreateRideDTO form = new CreateRideDTO("", "", LocalDateTime.now().plusHours(1), 3);
        model.addAttribute("createRideDTO", form);
        return "create-ride";
    }

    /**
     * Διαχειρίζεται την υποβολή της φόρμας δημιουργίας διαδρομής.
     */
    @PostMapping("/rides/create")
    public String handleCreateRide(
            @Valid @ModelAttribute("createRideDTO") CreateRideDTO dto,
            BindingResult bindingResult,
            Model model) {

        // Έλεγχος εγκυρότητας δεδομένων (Validation)
        if (bindingResult.hasErrors()) {
            return "create-ride"; // Επιστροφή στη φόρμα με τα σφάλματα
        }

        try {
            // Ανάκτηση του username του τρέχοντος συνδεδεμένου χρήστη
            String username = currentUserProvider.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Not authenticated"))
                    .username();

            // Δημιουργία της διαδρομής μέσω του Service
            rideService.createRide(dto, username);
            return "redirect:/rides?success=created"; // Ανακατεύθυνση με μήνυμα επιτυχίας

        } catch (Exception e) {
            // Διαχείριση επιχειρησιακών σφαλμάτων (π.χ. όριο ενεργών διαδρομών)
            model.addAttribute("error", e.getMessage());
            return "create-ride";
        }
    }

    /**
     * Διαχειρίζεται την κράτηση θέσης σε μια διαδρομή από το Web UI.
     * Χρησιμοποιεί POST request για την ενέργεια της κράτησης.
     */
    @PostMapping("/rides/{rideId}/book")
    public String handleWebBooking(@PathVariable Long rideId) {
        try {
            String username = currentUserProvider.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Not authenticated"))
                    .username();

            // Κλήση του BookingService για την εκτέλεση της κράτησης
            bookingService.bookRide(rideId, username);
            return "redirect:/my-bookings?success=booked";
        } catch (Exception e) {
            // Σε περίπτωση σφάλματος, επιστροφή στην αναζήτηση με το μήνυμα λάθους
            return "redirect:/rides?error=" + e.getMessage();
        }
    }

    /**
     * Εμφανίζει τις κρατήσεις του τρέχοντος χρήστη (Επιβάτης).
     */
    @GetMapping("/my-bookings")
    public String showMyBookings(Model model) {
        String username = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"))
                .username();

        var bookings = bookingService.getMyBookings(username);
        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }

    /**
     * Εμφανίζει τις διαδρομές που έχει προσφέρει ο τρέχων χρήστης (Οδηγός).
     */
    @GetMapping("/my-offered-rides")
    public String showMyOfferedRides(Model model) {
        String username = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"))
                .username();

        // Ανάκτηση διαδρομών όπου ο χρήστης είναι οδηγός
        List<Ride> rides = rideService.getRidesByDriver(username);

        // Μετατροπή σε DTOs
        List<RideView> views = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());

        model.addAttribute("rides", views);

        return "my-offered-rides";
    }
}
