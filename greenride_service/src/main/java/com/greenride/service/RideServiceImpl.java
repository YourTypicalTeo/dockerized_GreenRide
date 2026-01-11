package com.greenride.service;

import com.greenride.dto.CreateRideDTO;
import com.greenride.exception.ResourceNotFoundException; // Νέο Import
import com.greenride.model.Ride;
import com.greenride.model.User;
import com.greenride.repository.RideRepository;
import com.greenride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Η υλοποίησή μου για το RideService.
 * <p>
 * Εδώ έχω συγκεντρώσει όλη τη λογική για το πώς διαχειρίζομαι τις διαδρομές.
 * Φρόντισα να βάλω ελέγχους για να μην μπορεί κάποιος να δημιουργεί ανεξέλεγκτα διαδρομές
 * και να διασφαλίσω ότι τα δεδομένα είναι σωστά.
 * </p>
 */
@Service
public class RideServiceImpl implements RideService {

    /**
     * Όρισα το μέγιστο όριο στις 5 ενεργές διαδρομές.
     * Το έκανα αυτό για να αποφύγω περιπτώσεις spamming από οδηγούς.
     */
    private static final int MAX_ACTIVE_RIDES = 5;

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    @Autowired
    public RideServiceImpl(RideRepository rideRepository, UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    /**
     * Εδώ δημιουργώ μια νέα διαδρομή.
     * <p>
     * Χρησιμοποίησα το @Transactional για να είμαι σίγουρος ότι η εγγραφή θα γίνει σωστά στη βάση.
     * Πριν αποθηκεύσω, τσεκάρω αν ο οδηγός υπάρχει και αν έχει "χώρο" για νέα διαδρομή
     * με βάση το όριο που έθεσα παραπάνω.
     * </p>
     */
    @Override
    @Transactional
    public Ride createRide(CreateRideDTO dto, String driverUsername) {
        // Βήμα 1: Ψάχνω τον οδηγό στη βάση.
        User driver = userRepository.findByUsername(driverUsername)
                // ΑΛΛΑΓΗ: Εδώ προτίμησα να πετάξω το δικό μου Exception για να είναι πιο ξεκάθαρο το λάθος.
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverUsername));

        // Βήμα 2: Ελέγχω αν ο οδηγός έχει ξεπεράσει το όριο των διαδρομών.
        // Μετράω πόσες διαδρομές έχει προγραμματισμένες για το μέλλον.
        long activeRides = rideRepository.countByDriver_UsernameAndDepartureTimeAfter(driverUsername, LocalDateTime.now());
        
        // Αν έχει φτάσει το όριο, τον κόβω και επιστρέφω Bad Request.
        if (activeRides >= MAX_ACTIVE_RIDES) {
            // Θα μπορούσα να φτιάξω custom exception και εδώ, αλλά προς το παρόν το ResponseStatusException αρκεί.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot have more than " + MAX_ACTIVE_RIDES + " active rides. Please complete or cancel existing ones.");
        }

        // Βήμα 3: Ετοιμάζω το αντικείμενο Ride με τα δεδομένα από το DTO.
        Ride ride = new Ride();
        ride.setStartLocation(dto.startLocation());
        ride.setDestination(dto.destination());
        ride.setDepartureTime(dto.departureTime());
        ride.setAvailableSeats(dto.availableSeats());
        ride.setDriver(driver);

        // Βήμα 4: Το αποθηκεύω στη βάση.
        return rideRepository.save(ride);
    }

    /**
     * Η μέθοδος αναζήτησης.
     * Έκανα την αναζήτηση ευέλικτη (containing & ignoreCase) για να βρίσκει ο χρήστης εύκολα
     * αυτό που ψάχνει, ακόμα κι αν δεν γράψει ολόκληρο το όνομα της πόλης.
     */
    @Override
    public List<Ride> searchRides(String start, String destination) {
        return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCase(start, destination);
    }

    /**
     * Ανάκτηση διαδρομής με το ID.
     * Αν δεν βρω τη διαδρομή, πετάω το custom exception μου.
     */
    @Override
    public Ride getRideById(Long rideId) {
        return rideRepository.findById(rideId)
                // ΑΛΛΑΓΗ: Χρήση του δικού μου Exception
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));
    }

    /**
     * Βοηθητική μέθοδος για να πάρω όλες τις διαδρομές ενός συγκεκριμένου οδηγού.
     */
    @Override
    public List<Ride> getRidesByDriver(String driverUsername) {
        return rideRepository.findByDriver_Username(driverUsername);
    }

    // --- Admin Implementations ---

    /**
     * Επιστρέφω τα πάντα για τον Admin.
     */
    @Override
    public List<Ride> findAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public long countRides() {
        return rideRepository.count();
    }

    /**
     * Διαγραφή από Admin.
     * Πρόσθεσα έλεγχο ύπαρξης πριν τη διαγραφή για να αποφύγω σφάλματα αν το ID είναι λάθος.
     */
    @Override
    @Transactional
    public void adminDeleteRide(Long id) {
        if (!rideRepository.existsById(id)) {
            // Χρησιμοποιώ ξανά το ίδιο exception για συνέπεια στον κώδικα.
            throw new ResourceNotFoundException("Ride not found with ID: " + id);
        }
        rideRepository.deleteById(id);
    }
}
