package com.lms.controller;

import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.RegisterResponse;
import com.lms.entity.User;
import com.lms.security.JwtUtil;
import com.lms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User user = authService.register(req.getEmail(), req.getPassword());
        RegisterResponse response = new RegisterResponse(user.getId(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = authService.login(req.getEmail(), req.getPassword());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("Not logged in");
        return ResponseEntity.ok("Logged in as: " + authentication.getName());
    }
}
