package com.example.fintech_demo.controller;

import com.example.fintech_demo.dto.UserResponseDto;
import com.example.fintech_demo.exception.UserNotFoundException;
import com.example.fintech_demo.repository.UserRepository;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final OpenFgaClient fgaClient;

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable String id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(new UserResponseDto(u.getId(), u.getName(), u.getRole().name(), u.isActive())))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PostMapping("/fire/{userId}")
    public ResponseEntity<UserResponseDto> fire(@PathVariable String userId) throws Exception {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 1. Mark inactive in H2
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} marked inactive in DB", userId);

        // 2. Delete team membership tuple from OpenFGA — cascades to all downstream permissions
        fgaClient.deleteTuples(List.of(
                new ClientTupleKey()
                        .user("user:" + userId)
                        .relation("member")
                        ._object("team:finance")
        )).get();
        log.info("FGA tuple deleted — user:{} member team:finance", userId);

        return ResponseEntity.ok(new UserResponseDto(user.getId(), user.getName(), user.getRole().name(), user.isActive()));
    }
}

