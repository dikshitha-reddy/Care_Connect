package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("**")
public class AuthController {

    private final UserRepository repository;

    public AuthController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                request.getPassword(), // Storing plain text password
                request.getRole(),
                request.getPhone(),
                request.getSpecialization(),
                request.getExperience(),
                request.getFee()
        );

        repository.save(user);

        return ResponseEntity.ok(new AuthResponse(
                null, // No token
                user.getName(),
                user.getEmail(),
                user.getRole()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new AuthResponse(
                null, // No token
                user.getName(),
                user.getEmail(),
                user.getRole()
        ));
    }
}

