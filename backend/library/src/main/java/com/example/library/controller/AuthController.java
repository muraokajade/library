package com.example.library.controller;

import com.example.library.utils.ExtractJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login-check")
    public ResponseEntity<String> loginCheck(@RequestHeader("Authorization") String token) {
        String email = ExtractJWT.payloadJWTExtraction(token, "email");

        if(email != null) {
            return ResponseEntity.ok("ログイン成功" + email);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWTが無効");
        }
    }
}
