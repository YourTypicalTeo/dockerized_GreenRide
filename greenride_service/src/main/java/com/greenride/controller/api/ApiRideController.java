package com.greenride.controller.api;

import com.greenride.dto.CreateRideDTO;
import com.greenride.dto.RideView;
import com.greenride.model.Ride;
import com.greenride.security.CurrentUserProvider;
import com.greenride.service.BookingService;
import com.greenride.service.RideService;
import com.greenride.service.mapper.RideMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller για τη διαχείριση διαδρομών (Rides) και κρατήσεων (Bookings).
 * Όλα τα endpoints εδώ βρίσκονται κάτω από το /api/v1/rides.
 */
@RestController
@RequestMapping("/api/v1/rides")
@Tag(name = "Rides & Bookings", description = "Endpoints for managing rides and bookings")
// Δηλώνει ότι όλα τα endpoints απαιτούν JWT Token (Bearer Authentication) στο Swagger UI
@SecurityRequirement(name = "Bearer Authentication")
public class ApiRideController {

    private final RideService rideService;
    private final BookingService bookingService;
    private final CurrentUserProvider currentUserProvider;
    private final RideMapper rideMapper;

    // Dependency Injection μέσω του Constructor
    @Autowired
    public ApiRideController(RideService rideService,
                             BookingService bookingService,
                             CurrentUserProvider currentUserProvider,
                             RideMapper rideMapper) {
        this.rideService = rideService;
        this.bookingService = bookingService;
        this.currentUserProvider = currentUserProvider;
        this.rideMapper = rideMapper;
    }

    /**
     * Βοηθητική μέθοδος για την ανάκτηση του username του συνδεδεμένου χρήστη
     * από το Security Context (μέσω του token).
     */
    private String getCurrentUsername() {
        return currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Error: User is not authenticated."))
                .username();
    }

    /**
     * Endpoint για τη δημιουργία νέας διαδρομής.
     * Δέχεται JSON body (CreateRideDTO) και επιστρέφει το ID της νέας διαδρομής.
     */
    @Operation(summary = "Create a new Ride")
    @PostMapping
    public ResponseEntity<?> createRide(@Valid @RequestBody CreateRideDTO createRideDto) {
        String username = getCurrentUsername(); // Παίρνουμε τον οδηγό από το token
        Ride ride = rideService.createRide(createRideDto, username);
        return ResponseEntity.status(201).body("Ride created successfully with ID: " + ride.getId());
    }

    /**
     * Αναζήτηση διαδρομών με βάση την αφετηρία και τον προορισμό.
     * Επιστρέφει λίστα με RideView (DTOs) για να μην εκθέτουμε απευθείας τα Entities.
     */
    @Operation(summary = "Search for Rides")
    @GetMapping("/search")
    public ResponseEntity<List<RideView>> searchRides(
            @Parameter(description = "Starting city") @RequestParam String start,
            @Parameter(description = "Destination city") @RequestParam String dest) {

        List<Ride> rides = rideService.searchRides(start, dest);

        // Μετατροπή των Entities (Ride) σε DTOs (RideView)
        List<RideView> views = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());

        return ResponseEntity.ok(views);
    }

    /**
     * Ανάκτηση λεπτομερειών για μια συγκεκριμένη διαδρομή με βάση το ID της.
     */
    @Operation(summary = "Get Ride Details")
    @GetMapping("/{rideId}")
    public ResponseEntity<Map<String, Object>> getRideDetails(@PathVariable Long rideId) {
        Ride ride = rideService.getRideById(rideId);
        return ResponseEntity.ok(Map.of("ride", rideMapper.toRideView(ride)));
    }

    /**
     * Κράτηση θέσης σε μια διαδρομή.
     * Ο χρήστης ταυτοποιείται από το JWT token.
     */
    @Operation(summary = "Book a Seat")
    @PostMapping("/{rideId}/bookings")
    public ResponseEntity<String> bookSeat(@PathVariable Long rideId) {
        bookingService.bookRide(rideId, getCurrentUsername());
        return ResponseEntity.ok("Booking confirmed!");
    }

    /**
     * Ακύρωση κράτησης.
     * Ελέγχεται αν ο χρήστης που καλεί το endpoint είναι αυτός που έκανε την κράτηση.
     */
    @Operation(summary = "Cancel Booking")
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId, getCurrentUsername());
        return ResponseEntity.ok("Booking cancelled successfully. Seat restored.");
    }

    /**
     * Επιστρέφει τις διαδρομές που έχει προσφέρει ο τρέχων χρήστης (ως οδηγός).
     */
    @Operation(summary = "View Offered Rides")
    @GetMapping("/my-offered-rides")
    public ResponseEntity<List<RideView>> getMyOfferedRides() {
        List<Ride> rides = rideService.getRidesByDriver(getCurrentUsername());
        List<RideView> views = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());
        return ResponseEntity.ok(views);
    }

    /**
     * Επιστρέφει τις κρατήσεις που έχει κάνει ο τρέχων χρήστης (ως επιβάτης).
     */
    @Operation(summary = "View My Bookings")
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings(getCurrentUsername()));
    }
}
