package com.greenride.controller.api;

import com.greenride.dto.JwtResponse;
import com.greenride.dto.LoginRequest;
import com.greenride.dto.RegisterRequest;
import com.greenride.model.User;
import com.greenride.security.CustomUserDetails;
import com.greenride.security.JwtService;
import com.greenride.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
//Controller που διαχειρίζεται την αυθεντικοποίηση και εγγραφή χρηστών με REST API
// Παρέχει endpoints για δημιουργία λογαριασμού και σύνδεση με JWT token.
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for User Registration and Login")
public class ApiAuthController {

    @Autowired private UserService userService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    //Κανει εγγραφη τον νέο χρήστη
    //δέχεται τα στοιχεία του χρήστη, δημιουργεί με ρόλο ROLE_USER
    //επιστρέφει το ID του νέου χρήστη
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User newUser = userService.registerUser(registerRequest);
            return ResponseEntity.status(201).body("User registered successfully! User ID: " + newUser.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//Αυθεντικοποιεί έναν χρήστη και επιστρέφει JWT token.
//Ελέγχει (username/password), και αν είναι έγκυρα,
//παράγει ένα token που περιέχει τους ρόλους του χρήστη (Stateless Auth).
    @Operation(summary = "Login", description = "Authenticates a user and returns a sophisticated JWT token.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
            //Ενημερώνει οτι ο χρήστης είναι πλέον αυθεντικοποιημένος
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //ανάκτηση των χτοιχείων του user
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            //Εξαγωγή των ρόλων για να ενσωματωθούν στο Token
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            //Δίνει token με ρόλους xρησιμοποιώντας JwtService
            String jwt = jwtService.issue(userDetails.getUsername(), roles);

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Error: Authentication failed. " + e.getMessage());
        }
    }
}