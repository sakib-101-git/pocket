package com.pocket.pocket;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PlaidConfig {

    @Bean
    public PlaidApi plaidApi(
            @Value("${plaid.client-id}") String clientId,
            @Value("${plaid.secret}") String secret) {

        Map<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);

        ApiClient apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(ApiClient.Sandbox);

        return apiClient.createService(PlaidApi.class);
    }
}