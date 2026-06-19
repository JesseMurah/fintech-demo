package com.example.fintech_demo.service;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final OpenFgaClient fgaClient;

    public boolean check(String userId, String relation, String object) throws Exception {
        var request = new ClientCheckRequest()
                .user("user:" + userId)
                .relation(relation)
                ._object(object);

        boolean allowed = Boolean.TRUE.equals(fgaClient.check(request).get().getAllowed());
        log.info("FGA check — user:{} {} {} → {}", userId, relation, object, allowed);
        return allowed;
    }
}
