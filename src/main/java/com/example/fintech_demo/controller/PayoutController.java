package com.example.fintech_demo.controller;

import com.example.fintech_demo.dto.ApproveResponseDto;
import com.example.fintech_demo.exception.DisbursementNotFoundException;
import com.example.fintech_demo.exception.ForbiddenException;
import com.example.fintech_demo.repository.DisbursementRepository;
import com.example.fintech_demo.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayoutController {

    private final DisbursementRepository disbursementRepository;
    private final AuthorizationService authorizationService;

    // JWT path — trusts the role claim frozen in the token at mint time (the bug)
    @PostMapping("/jwt/payouts/{id}/approve")
    public ResponseEntity<ApproveResponseDto> approveJwt(
            @PathVariable String id,
            Authentication auth
    ) {
        disbursementRepository.findById(id)
                .orElseThrow(() -> new DisbursementNotFoundException(id));

        boolean hasRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FINANCE"));

        if (!hasRole) {
            throw new ForbiddenException(auth.getName(), "approve", "disbursement:" + id);
        }

        return ResponseEntity.ok(new ApproveResponseDto(id, auth.getName(), "Approved via JWT"));
    }

    // FGA path — re-derives authorization live from the graph on every request (the fix)
    @PostMapping("/fga/payouts/{id}/approve")
    public ResponseEntity<ApproveResponseDto> approveFga(
            @PathVariable String id,
            Authentication auth
    ) throws Exception {
        disbursementRepository.findById(id)
                .orElseThrow(() -> new DisbursementNotFoundException(id));

        if (!authorizationService.check(auth.getName(), "can_approve", "disbursement:" + id)) {
            throw new ForbiddenException(auth.getName(), "approve", "disbursement:" + id);
        }

        return ResponseEntity.ok(new ApproveResponseDto(id, auth.getName(), "Approved via FGA"));
    }
}
