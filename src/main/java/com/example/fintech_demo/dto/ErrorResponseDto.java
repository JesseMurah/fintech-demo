package com.example.fintech_demo.dto;

import java.time.Instant;

public record ErrorResponseDto(Instant timestamp, int status, String error, String message, String path) {}
