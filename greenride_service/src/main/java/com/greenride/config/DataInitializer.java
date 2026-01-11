package com.greenride.config;

import com.greenride.model.Ride;
import com.greenride.model.Role;
import com.greenride.model.User;
import com.greenride.repository.RideRepository;
import com.greenride.repository.RoleRepository;
import com.greenride.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      RideRepository rideRepository,

                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role userRole = createRoleIfNotFound(roleRepository, "ROLE_USER");
            Role adminRole = createRoleIfNotFound(roleRepository, "ROLE_ADMIN");


            // Φτιάχνουμε τον initial admin account
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@greenride.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setPhoneNumber("+306900000000");

                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                roles.add(adminRole);
                admin.setRoles(roles);

                userRepository.save(admin);
                System.out.println("ADMIN ACCOUNT CREATED: 'admin' / 'admin123'");
            }


            User driverMaria = createUserIfNotFound(userRepository, roleRepository, passwordEncoder,
                    "maria", "maria@test.com", "pass123", "+306911111111");

            User driverNikos = createUserIfNotFound(userRepository, roleRepository, passwordEncoder,
                    "nikos", "nikos@test.com", "pass123", "+306922222222");

            User passengerGiorgos = createUserIfNotFound(userRepository, roleRepository, passwordEncoder,
                    "giorgos", "giorgos@test.com", "pass123", "+306933333333");

            // φτιάχνει sample rides εαν δεν υπάρχουν ηδη
            if (rideRepository.count() == 0) {
                createRide(rideRepository, driverMaria,
                        "Athens, Syntagma",
                        "Thessaloniki, White Tower",
                        LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                        3);

                createRide(rideRepository, driverMaria,
                        "Athens, Omonoia",
                        "Chalkida",
                        LocalDateTime.now().plusDays(5).withHour(17).withMinute(30),
                        2);


                createRide(rideRepository, driverNikos,
                        "Patras, Port",
                        "Athens, Kifissia",
                        LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                        4);

                System.out.println("SAMPLE RIDES CREATED for Maria & Nikos");
            }
        };
    }

    private Role createRoleIfNotFound(RoleRepository roleRepository, String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private User createUserIfNotFound(UserRepository userRepository, RoleRepository roleRepository,
                                      PasswordEncoder passwordEncoder, String username, String email,
                                      String rawPassword, String phone) {
        //φτιαξε user με role εαν δεν υπάρχει ηδη
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setPhoneNumber(phone);

            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
            user.setRoles(Set.of(userRole));

            user.setEnabled(true);
            System.out.println("USER CREATED: " + username);
            return userRepository.save(user);
        });
    }

    private void createRide(RideRepository rideRepository, User driver, String start, String dest,
                            LocalDateTime deptTime, int seats) {
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setStartLocation(start);
        ride.setDestination(dest);
        ride.setDepartureTime(deptTime);
        ride.setAvailableSeats(seats);
        rideRepository.save(ride);
    }
}