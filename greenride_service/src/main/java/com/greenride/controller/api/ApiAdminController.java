package com.greenride.controller.api;

import com.greenride.model.User;
import com.greenride.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ApiAdminController {

    private final AdminService adminService;

    //κάνουμε inject μόνο το Service
    @Autowired
    public ApiAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        String responseMessage = adminService.toggleUserStatus(id);
        return ResponseEntity.ok(responseMessage);
    }
}