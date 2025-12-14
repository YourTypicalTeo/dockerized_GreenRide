package com.greenride.service.mapper;

import com.greenride.dto.RideView;
import com.greenride.dto.UserView;
import com.greenride.model.Ride;
import com.greenride.model.User;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {
    /**
     * Κανει ενα ride entity σε dto έτσι ώστε μονο τα απαραίτητα δεδομένα να ειναι φανερά στα API Clients
     *
     * @return Ενα RideView αντικείμενο με τα details του δώθεν Ride,
     *         Αλλίως Null εαν το Ride είναι null
     */
    public RideView toRideView(Ride ride) {
        if (ride == null) {
            return null;
        }

        User driver = ride.getDriver();
        UserView driverView = null;

        if (driver != null) {
            driverView = new UserView(
                    driver.getId(),
                    driver.getUsername(),
                    driver.getEmail()
            );
        }

        return new RideView(
                ride.getId(),
                ride.getStartLocation(),
                ride.getDestination(),
                ride.getDepartureTime(),
                ride.getAvailableSeats(),
                driverView
        );
    }
}