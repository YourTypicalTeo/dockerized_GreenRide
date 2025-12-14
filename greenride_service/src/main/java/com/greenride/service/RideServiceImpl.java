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

@Service
public class RideServiceImpl implements RideService {

    private static final int MAX_ACTIVE_RIDES = 5;

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    @Autowired
    public RideServiceImpl(RideRepository rideRepository, UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Ride createRide(CreateRideDTO dto, String driverUsername) {
        User driver = userRepository.findByUsername(driverUsername)
                // ΑΛΛΑΓΗ: Χρήση του δικού μας Exception αντί για HTTP Exception
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverUsername));

        long activeRides = rideRepository.countByDriver_UsernameAndDepartureTimeAfter(driverUsername, LocalDateTime.now());
        if (activeRides >= MAX_ACTIVE_RIDES) {
            // Εδώ μπορούμε να αφήσουμε το ResponseStatusException ως Business Logic error ή να φτιάξουμε και εδώ custom exception
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot have more than " + MAX_ACTIVE_RIDES + " active rides. Please complete or cancel existing ones.");
        }

        Ride ride = new Ride();
        ride.setStartLocation(dto.startLocation());
        ride.setDestination(dto.destination());
        ride.setDepartureTime(dto.departureTime());
        ride.setAvailableSeats(dto.availableSeats());
        ride.setDriver(driver);

        return rideRepository.save(ride);
    }

    @Override
    public List<Ride> searchRides(String start, String destination) {
        return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCase(start, destination);
    }

    @Override
    public Ride getRideById(Long rideId) {
        return rideRepository.findById(rideId)
                // ΑΛΛΑΓΗ: Χρήση του δικού μας Exception
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with ID: " + rideId));
    }

    @Override
    public List<Ride> getRidesByDriver(String driverUsername) {
        return rideRepository.findByDriver_Username(driverUsername);
    }
}