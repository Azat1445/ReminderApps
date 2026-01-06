package org.example.reminderapp.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reminderapp.exception.ErrorResponse;
import org.example.reminderapp.service.CustomUserDetailsService;
import org.example.reminderapp.service.JwtService;
import org.example.reminderapp.dto.response.AuthResponseDto;
import org.example.reminderapp.dto.request.LoginRequestDto;
import org.example.reminderapp.dto.request.UserCreateDto;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Role;
import org.example.reminderapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Tag(name = "Authentication", description = "Auth endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateDto request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            ErrorResponse error = new ErrorResponse(
                    OffsetDateTime.now(),
                    HttpStatus.CONFLICT.value(),
                    "Conflict",
                    "Username is already in use"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBirthDate(request.getBirthDate());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setRole(Role.USER);
        user.setCreatedAt(OffsetDateTime.now());

        userRepository.save(user);

        String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDto(jwt));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        String  jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponseDto(jwt));
    }

    @GetMapping("/success")
    public ResponseEntity<String> loginSuccess(@RequestParam("token") String token) {
        return ResponseEntity.ok("Your token is: " + token);
    }
}
