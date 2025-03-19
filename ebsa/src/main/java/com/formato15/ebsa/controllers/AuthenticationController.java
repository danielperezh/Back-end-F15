package com.formato15.ebsa.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;



import java.util.*;

import javax.crypto.SecretKey;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.beans.factory.annotation.Value;

// @CrossOrigin(origins = {
//     "http://formato15.ebsa.com.co:8080",
//     "http://formato15.ebsa.com.co:8086",
//     "https://formato15.ebsa.com.co:8082",
//     "http://localhost:8080"
// })
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration; 

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("usuario");
        String password = request.get("contrasena");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Usuario y contraseña son obligatorios."));
        }

        System.out.println("Intento de inicio de sesión para el usuario: " + username);

        if (validateOracleUser(username, password)) {
            String token = generateJwtToken(username);

            // Configurar el contexto de seguridad de Spring
            List<GrantedAuthority> authorities = List.of(); // Define roles si es necesario
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Confirmar autenticación exitosa
            System.out.println("Autenticación exitosa para el usuario: " + username);
            //System.out.println("Token generado: " + token);

            return ResponseEntity.ok(Map.of("success", true, "token", token));
        } else {
            System.out.println("Error: Autenticación fallida para el usuario: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Usuario o contraseña incorrectos."));
        }
    }

    private boolean validateOracleUser(String username, String password) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            System.out.println("Conexión exitosa a Oracle para el usuario: " + username);
            connection.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al conectar con Oracle: " + e.getMessage());
            System.out.println("Error al conectar con Oracle para el usuario: " + username);
            return false;
        }
    }

    private String generateJwtToken(String username) {
        // Generar una clave secreta
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        // Crear el token JWT
        String token = Jwts.builder()
                .setSubject(username)
                .claim("nombre", username)
                .setIssuedAt(new Date()) // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000)) // Expiración
                .signWith(key, SignatureAlgorithm.HS256) // Firma con la clave secreta
                .compact();

        System.out.println("JWT generado para el usuario: " + username);
        return token;
    }
}