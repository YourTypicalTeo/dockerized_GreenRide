package com.greenride.service;

import com.greenride.dto.RegisterRequest;
import com.greenride.model.User;
import java.util.List;

public interface UserService {
    User registerUser(RegisterRequest registerRequest);

    // --- Admin Methods ---
    List<User> findAllUsers();
    long countUsers();
    void deleteUser(Long id);
}