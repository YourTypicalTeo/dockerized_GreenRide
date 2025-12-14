package com.greenride.service;

import com.greenride.model.Role;
import com.greenride.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitializationService {

    private final RoleRepository roleRepository;

    @Autowired
    public InitializationService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initRoles() {
        //Κανει Check ενα υπάρχει ήδη ο ρόλος ROLE_USER αλλίως τον δημιουργεί
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
            System.out.println("SYSTEM: Created ROLE_USER in database.");
        }

        //Κανει Check ενα υπάρχει ήδη ο ρόλος ROLE_ADMIN αλλίως τον δημιουργεί
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            System.out.println("SYSTEM: Created ROLE_ADMIN in database.");
        }
    }
}