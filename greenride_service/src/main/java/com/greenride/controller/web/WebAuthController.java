package com.greenride.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebAuthController {

    @GetMapping("/login")
    public String login(Authentication authentication, HttpServletRequest request, Model model) {
        //Εαν εχει κανει ήδη login τον κάνουμε redirect στο home
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        //handers για errors
        if (request.getParameter("error") != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "login";
    }
}