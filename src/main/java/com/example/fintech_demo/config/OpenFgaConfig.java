package com.example.fintech_demo.config;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenFgaConfig {

    @Value("${app.openfga.api-url}")
    private String apiUrl;

    @Bean
    public OpenFgaClient openFgaClient() throws Exception {
        ClientConfiguration config = new ClientConfiguration()
                .apiUrl(apiUrl);
        return new OpenFgaClient(config);
    }
}
