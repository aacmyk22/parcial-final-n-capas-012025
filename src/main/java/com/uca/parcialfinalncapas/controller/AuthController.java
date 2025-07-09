package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.AuthRequest;
import com.uca.parcialfinalncapas.dto.response.AuthResponse;
import com.uca.parcialfinalncapas.security.jwt.JwtUtil;
import com.uca.parcialfinalncapas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Autenticar al usuario
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getCorreo(), request.getPassword());

        authManager.authenticate(authToken); // Lanza excepción si no es válido

        // Obtener detalles y generar token
        UserDetails userDetails = userService.loadUserByUsername(request.getCorreo());
        String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}

