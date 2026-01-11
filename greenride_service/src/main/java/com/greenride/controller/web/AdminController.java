package com.greenride.controller.web;

import com.greenride.service.AdminService;
import com.greenride.service.BlacklistService;
import com.greenride.service.RideService;
import com.greenride.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // <--- Άλλαξα σε wildcard (*) για να καλύψει το RequestParam

import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RideService rideService;
    private final AdminService adminService;
    private final BlacklistService blacklistService;

    @Autowired
    public AdminController(UserService userService, RideService rideService, AdminService adminService, BlacklistService blacklistService) { // <--- Προσθήκη στον Constructor
        this.userService = userService;
        this.rideService = rideService;
        this.adminService = adminService;
        this.blacklistService = blacklistService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        // 1. Παίρνουμε ΟΛΑ τα στατιστικά (μαζί με το Occupancy) από το Service
        Map<String, Object> stats = adminService.getSystemStats();

        model.addAttribute("totalUsers", stats.get("totalUsers"));
        model.addAttribute("totalRides", stats.get("totalRidesOffered"));

        // Περνάμε το νέο στατιστικό στο HTML
        model.addAttribute("occupancy", stats.get("averageOccupancyPercent"));

        // 2. Λίστες για τους πίνακες
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("rides", rideService.findAllRides());

        //Περνάμε τη λίστα με τις IP στο HTML
        model.addAttribute("blockedIps", blacklistService.getBlockedIps());

        return "admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin?success=User Deleted";
    }

    @PostMapping("/rides/{id}/delete")
    public String deleteRide(@PathVariable Long id) {
        rideService.adminDeleteRide(id);
        return "redirect:/admin?success=Ride Deleted";
    }

    @PostMapping("/blacklist/add")
    public String blockIp(@RequestParam String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            blacklistService.blockIp(ip.trim());
        }
        return "redirect:/admin";
    }

    @PostMapping("/blacklist/remove")
    public String unblockIp(@RequestParam String ip) {
        blacklistService.unblockIp(ip);
        return "redirect:/admin";
    }
}