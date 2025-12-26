package com.greenride.service;

import com.greenride.model.Ride;
import com.greenride.model.User;
import com.greenride.repository.BookingRepository;
import com.greenride.repository.RideRepository;
import com.greenride.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            RideRepository rideRepository,
                            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Βασικά Στατιστικά
        long totalUsers = userRepository.count();
        long totalRides = rideRepository.count();
        long totalBookings = bookingRepository.count(); // Ενεργές κρατήσεις

        stats.put("totalUsers", totalUsers);
        stats.put("totalRidesOffered", totalRides);
        stats.put("totalBookingsMade", totalBookings);

        // 2. Υπολογισμός Μέσου Όρου Πληρότητας (Average Occupancy)
        double averageOccupancy = 0.0;

        if (totalRides > 0) {
            // Υπολογίζουμε τις συνολικές θέσεις που είναι ΑΚΟΜΑ διαθέσιμες σε όλες τις διαδρομές
            long totalAvailableSeats = rideRepository.findAll().stream()
                    .mapToLong(Ride::getAvailableSeats)
                    .sum();

            // Η πραγματική συνολική χωρητικότητα του στόλου είναι:
            // Κατειλημμένες θέσεις (Bookings) + Κενές θέσεις (AvailableSeats)
            long totalCapacity = totalBookings + totalAvailableSeats;

            if (totalCapacity > 0) {
                // Ποσοστό πληρότητας = Κατειλημμένες / Συνολικές
                averageOccupancy = (double) totalBookings / totalCapacity;
            }
        }

        // Επιστρέφουμε το ποσοστό (π.χ. 0.45 για 45%)
        stats.put("averageOccupancy", averageOccupancy);

        // Μπορούμε να προσθέσουμε και μια πιο φιλική μορφή κειμένου (π.χ. "45%")
        stats.put("averageOccupancyPercent", String.format("%.1f%%", averageOccupancy * 100));

        return stats;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public String toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        return "User status updated. Enabled: " + user.isEnabled();
    }
}