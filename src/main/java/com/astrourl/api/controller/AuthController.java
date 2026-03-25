package com.astrourl.api.controller;

import com.astrourl.api.config.JwtService;
import com.astrourl.api.dto.AuthRequestDTO;
import com.astrourl.api.model.User;
import com.astrourl.api.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Constructor manual para Inyección de Dependencias (reemplaza a Lombok)
    public AuthController(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody AuthRequestDTO request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        repository.save(user);
        return Map.of("message", "Astronauta registrado con éxito");
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AuthRequestDTO request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);
        return Map.of("token", token);
    }
}