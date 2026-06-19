package com.example.fintech_demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FgaBootstrap implements CommandLineRunner {

    private final OpenFgaClient fgaClient;

    @Override
    public void run(String... args) throws Exception {
        // 1. Create store
        var store = fgaClient.createStore(new CreateStoreRequest().name("fintech-demo")).get();
        fgaClient.setStoreId(store.getId());
        log.info("FGA store created: {}", store.getId());

        // 2. Write authorization model from model.json (converted from model.fga via ADR-0001)
        var modelJson = new ClassPathResource("fga/model.json").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        var modelRequest = new ObjectMapper().readValue(modelJson, WriteAuthorizationModelRequest.class);
        var modelResponse = fgaClient.writeAuthorizationModel(modelRequest).get();
        fgaClient.setAuthorizationModelId(modelResponse.getAuthorizationModelId());
        log.info("FGA model written: {}", modelResponse.getAuthorizationModelId());

        // 3. Seed relationship tuples
        fgaClient.write(new ClientWriteRequest().writes(List.of(
                new ClientTupleKey().user("user:kwame").relation("member")._object("team:finance"),
                new ClientTupleKey().user("team:finance#member").relation("approver")._object("loan:L1"),
                new ClientTupleKey().user("loan:L1").relation("parent")._object("disbursement:D1"),
                new ClientTupleKey().user("loan:L1").relation("parent")._object("disbursement:D2")
        ))).get();
        log.info("FGA tuples seeded — user:kwame → team:finance → loan:L1 → D1, D2");
    }
}
