package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.dto.RegisterRequest;
import com.fooddeliveryapp.api.services.AuthService;
import com.fooddeliveryapp.api.models.User;
import com.fooddeliveryapp.api.repositories.UserRepository;
import com.fooddeliveryapp.api.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, AuthService authService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        Optional<User> userOptional = userRepository.findByEmail(loginUser.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Since we upgraded to BCrypt, we match using passwordEncoder.
            if (passwordEncoder.matches(loginUser.getPassword(), user.getPassword()) || 
                user.getPassword().equals(loginUser.getPassword())) {  // Fallback for old plain-text passwords
                Map<String, String> response = new HashMap<>();
                response.put("id", String.valueOf(user.getId()));
                response.put("name", user.getName() != null ? user.getName() : "");
                response.put("email", user.getEmail());
                response.put("phone", user.getPhone() != null ? user.getPhone() : "");
                response.put("address", user.getAddress() != null ? user.getAddress() : "");
                response.put("role", user.getRole() != null ? user.getRole().name() : "CUSTOMER");
                response.put("token", jwtService.issueToken(user.getId(), user.getRole()));
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request);
            Map<String, String> response = new HashMap<>();
            response.put("id", String.valueOf(user.getId()));
            response.put("name", user.getName() != null ? user.getName() : "");
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone() != null ? user.getPhone() : "");
            response.put("address", user.getAddress() != null ? user.getAddress() : "");
            response.put("role", user.getRole() != null ? user.getRole().name() : "CUSTOMER");
            response.put("token", jwtService.issueToken(user.getId(), user.getRole()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
