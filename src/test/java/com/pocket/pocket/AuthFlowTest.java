package com.pocket.pocket;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void fullAuthFlow_registerLoginAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"flow@example.com\",\"password\":\"testpass123\"}")).andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"flow@example.com\",\"password\":\"testpass123\"}")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Map<String, String> responseMap = objectMapper.readValue(loginResponse, Map.class);
        String token = responseMap.get("token");

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_rejectsRequestWithNoToken() throws Exception {
        mockMvc.perform(get("/me")).andExpect(status().isForbidden());
    }
}
