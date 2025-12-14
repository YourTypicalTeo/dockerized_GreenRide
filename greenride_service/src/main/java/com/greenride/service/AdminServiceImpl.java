package com.greenride.service;

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
    //inject controllers
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
        stats.put("totalUsers", userRepository.count());
        stats.put("totalRidesOffered", rideRepository.count());
        stats.put("totalBookingsMade", bookingRepository.count());
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