package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.User;
import com.fooddeliveryapp.api.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        Optional<User> userOptional = userRepository.findByEmail(loginUser.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(loginUser.getPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("id", String.valueOf(user.getId()));
                response.put("name", user.getName() != null ? user.getName() : "");
                response.put("email", user.getEmail());
                response.put("phone", user.getPhone() != null ? user.getPhone() : "");
                response.put("address", user.getAddress() != null ? user.getAddress() : "");
                response.put("role", user.getRole() != null ? user.getRole() : "customer");
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User savedUser = userRepository.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(savedUser.getId()));
        response.put("name", savedUser.getName() != null ? savedUser.getName() : "");
        response.put("email", savedUser.getEmail());
        response.put("phone", savedUser.getPhone() != null ? savedUser.getPhone() : "");
        response.put("address", savedUser.getAddress() != null ? savedUser.getAddress() : "");
        response.put("role", savedUser.getRole() != null ? savedUser.getRole() : "customer");
        return ResponseEntity.ok(response);
    }
}
