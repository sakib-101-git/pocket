package com.pocket.pocket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TransactionControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"testpass123\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Map<String, String> map = jsonMapper.readValue(response, Map.class);
        return map.get("token");
    }

    @Test
    void returnsPaginatedTransactionsForOwner() throws Exception {
        User user = new User();
        user.setEmail("txn-page-owner@example.com");
        user.setPasswordHash(passwordEncoder.encode("testpass123"));
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setName("Checking");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        for (int i = 0; i < 3; i++) {
            Transaction t = new Transaction();
            t.setAccount(account);
            t.setAmount(new BigDecimal("10.00"));
            t.setDescription("Test txn " + i);
            t.setTransactionDate(LocalDateTime.now());
            t.setCreatedAt(LocalDateTime.now());
            transactionRepository.save(t);
        }

        String token = loginAndGetToken("txn-page-owner@example.com");

        mockMvc.perform(get("/accounts/" + account.getId() + "/transactions?page=0&size=2")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void rejectsAccessToAnotherUsersAccount() throws Exception {
        User owner = new User();
        owner.setEmail("txn-real-owner@example.com");
        owner.setPasswordHash(passwordEncoder.encode("testpass123"));
        owner.setCreatedAt(LocalDateTime.now());
        owner = userRepository.save(owner);

        Account account = new Account();
        account.setUser(owner);
        account.setName("Checking");
        account.setAccountType("checking");
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        User intruder = new User();
        intruder.setEmail("txn-intruder@example.com");
        intruder.setPasswordHash(passwordEncoder.encode("testpass123"));
        intruder.setCreatedAt(LocalDateTime.now());
        userRepository.save(intruder);

        String intruderToken = loginAndGetToken("txn-intruder@example.com");

        mockMvc.perform(get("/accounts/" + account.getId() + "/transactions")
                .header("Authorization", "Bearer " + intruderToken))
            .andExpect(status().isNotFound());
    }
}
