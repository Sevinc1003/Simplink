package com.simplink.main;

import com.simplink.main.dto.UrlRequest;
import com.simplink.main.entity.Url;
import com.simplink.main.repository.IpLogRepository;
import com.simplink.main.repository.UrlRepository;
import com.simplink.main.repository.UserRepository;
import com.simplink.main.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private IpLogRepository ipLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UrlService urlService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        ipLogRepository.deleteAll();
        urlRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRedirectToOriginalUrl() throws Exception {
        String token = registerAndGetToken("redirect@test.com");
        String originalUrl = "https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H";

        String shortUrl = createShortUrl(originalUrl, token);
        String shortCode = extractShortCode(shortUrl);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(ipLogRepository.count() >= 1));
    }

    @Test
    void shouldCreateShortUrlWhenAuthenticated() throws Exception {
        String token = registerAndGetToken("create@test.com");

        String shortUrl = createShortUrl(
                "https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H",
                token
        );

        assertFalse(shortUrl.isEmpty());
        assertTrue(shortUrl.contains("http://localhost"));
    }

    @Test
    void shouldRejectCreateShortUrlWithoutToken() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://google.com");

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnClickCountForOwner() throws Exception {
        String token = registerAndGetToken("clicks@test.com");

        String shortUrl = createShortUrl("https://google.com", token);
        String shortCode = extractShortCode(shortUrl);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound());

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(ipLogRepository.count() >= 1));

        mockMvc.perform(get("/urls/" + shortCode + "/clicks")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void shouldReturnIpLogsForOwner() throws Exception {
        String token = registerAndGetToken("logs@test.com");

        String shortUrl = createShortUrl("https://google.com", token);
        String shortCode = extractShortCode(shortUrl);

        mockMvc.perform(get("/" + shortCode)
                        .header("User-Agent", "JUnit Test Agent"))
                .andExpect(status().isFound());

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(ipLogRepository.count() >= 1));

        mockMvc.perform(get("/urls/" + shortCode + "/ip-logs")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateUrlForOwner() throws Exception {
        String token = registerAndGetToken("update@test.com");

        createShortUrl("https://google.com", token);

        Url savedUrl = urlRepository.findAll().get(0);

        UrlRequest request = new UrlRequest();
        request.setUrl("https://github.com");

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/urls/" + savedUrl.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://github.com"));
    }

    @Test
    void shouldDeleteUrlForOwner() throws Exception {
        String token = registerAndGetToken("delete@test.com");

        createShortUrl("https://google.com", token);

        Url savedUrl = urlRepository.findAll().get(0);

        mockMvc.perform(delete("/api/urls/" + savedUrl.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        assertTrue(urlRepository.findById(savedUrl.getId()).isEmpty());
    }

    @Test
    void shouldForbidUpdateUrlForAnotherUser() throws Exception {
        String ownerToken = registerAndGetToken("owner@test.com");
        String anotherUserToken = registerAndGetToken("another@test.com");

        createShortUrl("https://google.com", ownerToken);

        Url savedUrl = urlRepository.findAll().get(0);

        UrlRequest request = new UrlRequest();
        request.setUrl("https://github.com");

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/urls/" + savedUrl.getId())
                        .header("Authorization", bearer(anotherUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email) throws Exception {
        String jsonRequest = """
                {
                  "email": "%s",
                  "password": "123456"
                }
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        return extractToken(result);
    }

    private String createShortUrl(String originalUrl, String token) throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl(originalUrl);

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/urls")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String extractShortCode(String shortUrl) {
        return shortUrl.substring(shortUrl.lastIndexOf("/") + 1);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extractToken(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();

        int tokenStart = responseBody.indexOf("\"token\":\"") + 9;
        int tokenEnd = responseBody.indexOf("\"", tokenStart);

        return responseBody.substring(tokenStart, tokenEnd);
    }
}