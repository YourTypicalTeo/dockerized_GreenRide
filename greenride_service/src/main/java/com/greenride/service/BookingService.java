package com.greenride.service;

import com.greenride.model.Booking;
import com.greenride.model.Ride;
import com.greenride.model.User;
import com.greenride.repository.BookingRepository;
import com.greenride.repository.RideRepository;
import com.greenride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service που διαχειρίζεται όλη τη λογική των κρατήσεων.
 * Εδώ εφαρμόζονται οι επιχειρησιακοί κανόνες (Business Logic) και η διαχείριση ταυτόχρονων χρηστών.
 */
@Service
public class BookingService {

    @Autowired private RideRepository rideRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Δημιουργία νέας κράτησης.
     * Χρησιμοποιεί @Transactional για να διασφαλίσει ότι όλα θα γίνουν σωστά ή τίποτα (ACID).
     */
    @Transactional
    public void bookRide(Long rideId, String passengerUsername) {
        // 1. Γρήγορος έλεγχος: Υπάρχει ο χρήστης που πάει να κάνει κράτηση;
        User passenger = userRepository.findByUsername(passengerUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Γρήγορος έλεγχος: Υπάρχει η διαδρομή;
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        // 3. Κανόνας: Ο οδηγός δεν επιτρέπεται να κλείσει θέση στη δική του διαδρομή.
        if (ride.getDriver().getId().equals(passenger.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drivers cannot book their own ride.");
        }

        // 4. Κανόνας: Έλεγχος για διπλο-εγγραφές. Αν ο χρήστης έχει ήδη ενεργή κράτηση, απορρίπτουμε.
        boolean alreadyBooked = ride.getBookings().stream()
                .anyMatch(b -> b.getPassenger().getId().equals(passenger.getId())
                        && !"CANCELLED".equals(b.getStatus()));
        if (alreadyBooked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this ride.");
        }

        /* 5. --- ΚΡΙΣΙΜΟ ΣΗΜΕΙΟ CONCURRENCY (Ταυτοχρονισμός) ---
         Εδώ λύνουμε το πρόβλημα του "Race Condition".
         Αντί να διαβάσουμε τις θέσεις (getAvailableSeats) και να τις μειώσουμε στη μνήμη της Java,
         στέλνουμε ένα ατομικό ερώτημα UPDATE στη βάση δεδομένων:
         "Μείωσε κατά 1  αν-αν οι θέσεις είναι > 0".
         Η μέθοδος επιστρέφει πόσες γραμμές επηρεάστηκαν (1 = επιτυχία, 0 = αποτυχία). */
        int updatedRows = rideRepository.decrementAvailableSeats(rideId);
        
        // Αν επιστρέψει 0, σημαίνει ότι κάποιος άλλος πρόλαβε την τελευταία θέση χιλιοστά του δευτερολέπτου πριν.
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is fully booked!");
        }

        // 6. Αν περάσαμε το βήμα 5, η θέση είναι δεσμευμένη. Αποθηκεύουμε την κράτηση.
        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setPassenger(passenger);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED"); // Αρχική κατάσταση

        bookingRepository.save(booking);
    }

    /**
     * Επιστρέφει τις κρατήσεις ενός συγκεκριμένου χρήστη.
     */
    public List<Booking> getMyBookings(String username) {
        return bookingRepository.findByPassenger_Username(username);
    }

    /**
     * Ακύρωση κράτησης.
     *  
     */
    @Transactional
    public void cancelBooking(Long bookingId, String username) {
        // 1. Εύρεση της κράτησης
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        /* 2. Security Check: Βεβαιωνόμαστε ότι αυτός που πατάει "Ακύρωση" είναι όντως ο επιβάτης.
         (Για να μην μπορεί κάποιος να ακυρώσει ξένες κρατήσεις αλλάζοντας το ID στο URL).*/
        if (!booking.getPassenger().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to cancel this booking.");
        }

        // 3. Έλεγχος αν είναι ήδη ακυρωμένη
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already cancelled.");
        }

        /* 4. Επιχειρησιακός Κανόνας: Απαγόρευση ακύρωσης τελευταία στιγμή.
         Υπολογίζουμε τα λεπτά που απομένουν μέχρι την αναχώρηση. */
        LocalDateTime departureTime = booking.getRide().getDepartureTime();
        long minutesUntilDeparture = Duration.between(LocalDateTime.now(), departureTime).toMinutes();

        // Αν απομένουν λιγότερο από 10 λεπτά, πετάμε λάθος.
        if (minutesUntilDeparture < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel within 10 minutes of departure.");
        }

        // 5. Μαρκάρισμα της κράτησης ως Ακυρωμένης
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // 6. --- CONCURRENCY FIX  ---
        /* Ατομική αύξηση των θέσεων. Επιστρέφουμε τη θέση πίσω στο σύστημα
         ώστε να μπορεί να την κλείσει κάποιος άλλος χρήστης.*/
        rideRepository.incrementAvailableSeats(booking.getRide().getId());
    }
}
