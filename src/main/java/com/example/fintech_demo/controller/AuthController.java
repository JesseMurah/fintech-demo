package com.example.fintech_demo.controller;

import com.example.fintech_demo.dto.LoginResponseDto;
import com.example.fintech_demo.dto.LoginUserDto;
import com.example.fintech_demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginUserDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
