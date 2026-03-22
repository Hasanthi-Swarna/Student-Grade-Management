package com.hasanthi.grades.controller;

import com.hasanthi.grades.dto.AuthDtos.*;
import com.hasanthi.grades.model.User;
import com.hasanthi.grades.repository.UserRepository;
import com.hasanthi.grades.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (TEACHER or STUDENT role)")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User user = User.builder()
                .name(req.name)
                .email(req.email)
                .password(passwordEncoder.encode(req.password))
                .role(req.role)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password));
        UserDetails ud = userDetailsService.loadUserByUsername(req.email);
        String token = jwtUtils.generateToken(ud);
        User user = userRepository.findByEmail(req.email).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name(), user.getName()));
    }
}
