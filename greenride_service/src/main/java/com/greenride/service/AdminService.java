package com.greenride.service;

import com.greenride.model.User;
import java.util.List;
import java.util.Map;
//AdminService Για να μην εχουμε τα Repositories injected κατευθειαν στον Controller
public interface AdminService {
    Map<String, Object> getSystemStats();
    List<User> getAllUsers();
    String toggleUserStatus(Long userId);
}