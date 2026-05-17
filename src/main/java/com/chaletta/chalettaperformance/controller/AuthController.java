package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.dto.AuthResponse;
import com.chaletta.chalettaperformance.dto.LoginRequest;
import com.chaletta.chalettaperformance.dto.RegisterRequest;
import com.chaletta.chalettaperformance.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login a user.
     * @param request The Login Request Body.
     * @return AuthResponse.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

}
