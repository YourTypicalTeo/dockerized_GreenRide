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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
// ΔΙΟΡΘΩΣΗ: Αφαιρέσαμε το @RequestMapping("/rides") από εδώ
public class WebRideController {

    private final RideService rideService;
    private final BookingService bookingService;
    private final RideMapper rideMapper;
    private final CurrentUserProvider currentUserProvider;

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

    // --- RIDE LISTING ---

    // ΔΙΟΡΘΩΣΗ: Προσθέσαμε το "/rides" εδώ
    @GetMapping("/rides")
    public String showRides(
            @RequestParam(required = false, defaultValue = "") String start,
            @RequestParam(required = false, defaultValue = "") String dest,
            Model model) {

        List<Ride> rides = rideService.searchRides(start, dest);

        List<RideView> rideViews = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());

        model.addAttribute("rides", rideViews);
        model.addAttribute("paramStart", start);
        model.addAttribute("paramDest", dest);

        return "rides";
    }

    // --- CREATE RIDE ---

    // ΔΙΟΡΘΩΣΗ: Προσθέσαμε το "/rides/create" εδώ
    @GetMapping("/rides/create")
    public String showCreateRideForm(Model model) {
        CreateRideDTO form = new CreateRideDTO("", "", LocalDateTime.now().plusHours(1), 3);
        model.addAttribute("createRideDTO", form);
        return "create-ride";
    }

    // ΔΙΟΡΘΩΣΗ: Προσθέσαμε το "/rides/create" εδώ
    @PostMapping("/rides/create")
    public String handleCreateRide(
            @Valid @ModelAttribute("createRideDTO") CreateRideDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "create-ride";
        }

        try {
            String username = currentUserProvider.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Not authenticated"))
                    .username();

            rideService.createRide(dto, username);
            return "redirect:/rides?success=created";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "create-ride";
        }
    }

    // --- BOOKING ---

    // ΔΙΟΡΘΩΣΗ: Προσθέσαμε το "/rides/{id}/book" εδώ
    @PostMapping("/rides/{id}/book")
    public String bookRide(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            // Η σωστή μέθοδος είναι bookRide
            bookingService.bookRide(id, userDetails.getUsername());

            redirectAttributes.addFlashAttribute("successMessage", "Booking successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + e.getMessage());
        }

        return "redirect:/rides";
    }

    // --- MY BOOKINGS & OFFERED (Αυτά μένουν ως έχουν στο root path) ---

    @GetMapping("/my-bookings")
    public String showMyBookings(Model model) {
        String username = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"))
                .username();

        var bookings = bookingService.getMyBookings(username);
        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }

    @GetMapping("/my-offered-rides")
    public String showMyOfferedRides(Model model) {
        String username = currentUserProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"))
                .username();

        List<Ride> rides = rideService.getRidesByDriver(username);

        List<RideView> views = rides.stream()
                .map(rideMapper::toRideView)
                .collect(Collectors.toList());

        model.addAttribute("rides", views);

        return "my-offered-rides";
    }
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            // Καλούμε το Service με το username του χρήστη
            bookingService.cancelBooking(id, userDetails.getUsername());

            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancellation failed: " + e.getMessage());
        }

        return "redirect:/my-bookings";
    }
}