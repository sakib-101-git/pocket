package com.pocket.pocket;

import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.request.PlaidApi;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

@RestController
public class LinkTokenController {

    private final PlaidApi plaidApi;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public LinkTokenController(PlaidApi plaidApi, UserRepository userRepository, AccountRepository accountRepository) {
        this.plaidApi = plaidApi;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/accounts/link/token")
    public Map<String, String> createLinkToken() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(new LinkTokenCreateRequestUser().clientUserId(String.valueOf(user.getId())))
                .clientName("Pocket")
                .products(List.of(Products.TRANSACTIONS))
                .countryCodes(List.of(CountryCode.US))
                .language("en");

        Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(request).execute();

        if (!response.isSuccessful()) {
            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new RuntimeException("Plaid link token creation failed: " + errorMessage);
        }

        return Map.of("linkToken", response.body().getLinkToken());
    }

    @PostMapping("/accounts/link/exchange")
    public AccountResponse exchangeToken(@RequestBody ExchangeTokenRequest request) throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        ItemPublicTokenExchangeRequest exchangeRequest
                = new ItemPublicTokenExchangeRequest().publicToken(request.getPublicToken());

        Response<ItemPublicTokenExchangeResponse> response
                = plaidApi.itemPublicTokenExchange(exchangeRequest).execute();

        if (!response.isSuccessful()) {
            String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new RuntimeException("Plaid token exchange failed: " + errorMessage);
        }

        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        Account account = new Account();
        account.setUser(user);
        account.setName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setCreatedAt(java.time.LocalDateTime.now());
        account.setPlaidAccessToken(accessToken);
        account.setPlaidItemId(itemId);

        Account saved = accountRepository.save(account);
        return new AccountResponse(saved.getId(), saved.getName(), saved.getAccountType());
    }
}
