package com.example.fintech_demo.service;

import com.example.fintech_demo.dto.LoginResponseDto;
import com.example.fintech_demo.dto.LoginUserDto;
import com.example.fintech_demo.exception.UserNotFoundException;
import com.example.fintech_demo.model.User;
import com.example.fintech_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginUserDto request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        String token = jwtService.generateToken(user.getId(), user.getRole().name());
        return new LoginResponseDto(token);
    }
}
