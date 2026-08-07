package com.pocket.pocket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"testpass123\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, String> map = jsonMapper.readValue(response, Map.class);
        return map.get("token");
    }

    @Test
    void userOnlySeesTheirOwnAccounts() throws Exception {
        User userA = new User();
        userA.setEmail("owner-a@example.com");
        userA.setPasswordHash(passwordEncoder.encode("testpass123"));
        userA.setCreatedAt(LocalDateTime.now());
        userA = userRepository.save(userA);

        User userB = new User();
        userB.setEmail("owner-b@example.com");
        userB.setPasswordHash(passwordEncoder.encode("testpass123"));
        userB.setCreatedAt(LocalDateTime.now());
        userRepository.save(userB);

        Account accountA = new Account();
        accountA.setUser(userA);
        accountA.setName("A's Checking");
        accountA.setAccountType("checking");
        accountA.setCreatedAt(LocalDateTime.now());
        accountRepository.save(accountA);

        String tokenB = loginAndGetToken("owner-b@example.com");

        mockMvc.perform(get("/accounts")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isEmpty());
    }
}
