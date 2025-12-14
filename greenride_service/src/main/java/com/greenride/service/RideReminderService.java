package com.greenride.service;

import com.greenride.model.Booking;
import com.greenride.repository.BookingRepository;
import com.greenride.service.port.SmsNotificationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideReminderService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private SmsNotificationPort smsNotificationPort;

    /**
     * Τρέχει κάθε 1 λεπτό (60000ms).
     * Ελέγχει αν υπάρχουν διαδρομές που ξεκινάνε σε λιγότερο από 1 ώρα.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendUpcomingRideReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Booking> bookings = bookingRepository.findAll();

        for (Booking booking : bookings) {
            // Αν η κράτηση έχει ακυρωθεί, την αγνοούμε
            if ("CANCELLED".equals(booking.getStatus())) {
                continue;
            }

            LocalDateTime departure = booking.getRide().getDepartureTime();

            // Λογική ελέγχου:
            // 1. Είναι η αναχώρηση στο μέλλον; (after now)
            // 2. Είναι η αναχώρηση μέσα στην επόμενη ώρα; (before oneHourLater)
            // 3. Έχουμε ΗΔΗ στείλει υπενθύμιση; (reminderSent flag) --> Αποφυγή Spamming
            if (departure.isAfter(now) &&
                    departure.isBefore(oneHourLater) &&
                    !booking.isReminderSent()) { 

                System.out.println("⏰ REMINDER SENT: Ride for " + booking.getPassenger().getUsername());

                // Χρήση του SMS Port για αποστολή μηνύματος
                smsNotificationPort.sendSms(booking.getPassenger().getPhoneNumber(),
                        "Reminder: Your ride to " + booking.getRide().getDestination() + " leaves in less than an hour!");

                // UPDATE STATE: Μαρκάρουμε ότι στάλθηκε για να μην ξανασταλεί στο επόμενο λεπτό
                booking.setReminderSent(true);
                bookingRepository.save(booking);
            }
        }
    }
}
