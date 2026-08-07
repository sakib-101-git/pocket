package com.pocket.pocket;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.json.JsonMapper;

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

    @Test
    void createsAccountWithValidInput() throws Exception {
        User user = new User();
        user.setEmail("create-account@example.com");
        user.setPasswordHash(passwordEncoder.encode("testpass123"));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = loginAndGetToken("create-account@example.com");

        mockMvc.perform(post("/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"My Savings\",\"accountType\":\"savings\"}"))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value("My Savings"));
    }

    @Test
    void rejectsInvalidAccountType() throws Exception {
        User user = new User();
        user.setEmail("bad-account@example.com");
        user.setPasswordHash(passwordEncoder.encode("testpass123"));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = loginAndGetToken("bad-account@example.com");

        mockMvc.perform(post("/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"accountType\":\"banana\"}"))
                .andExpect(status().isBadRequest());
    }
}
