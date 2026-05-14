package com.simplink.main;

import com.simplink.main.dto.UrlRequest;
import com.simplink.main.repository.IpLogRepository;
import com.simplink.main.repository.UrlRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private UrlService urlService;

    @BeforeEach
    void setup() {
        ipLogRepository.deleteAll();
        urlRepository.deleteAll();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRedirectToOriginalUrl() throws Exception {
        String originalUrl = "https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H";
        String generatedShortCode = urlService.shortenUrl(originalUrl);

        mockMvc.perform(get("/" + generatedShortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(1, ipLogRepository.count(),
                            "Expected 1 log entry after redirect.");
                });
    }

    @Test
    void shouldCreateShortUrl() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H");
        String jsonRequest = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assert !responseBody.isEmpty() : "Response URL should not be empty.";
                    assert responseBody.contains("http://localhost") : "Response must contain the base URL.";
                });
    }

    @Test
    void shouldReturnClickCount() throws Exception{
        UrlRequest request = new UrlRequest();
        request.setUrl("https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H");
        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult createResult = mockMvc.perform(post("/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String shortUrl = createResult.getResponse().getContentAsString();
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound());

        mockMvc.perform(get("/urls/" + shortCode + "/clicks"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

    }
    @Test
    void shouldReturnIpLogs() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://youtu.be/n-5P6UJNQZU?si=g3ik6hKkqUUsTj9H");
        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult createResult = mockMvc.perform(post("/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        String shortUrl = createResult.getResponse().getContentAsString();
        String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);


        mockMvc.perform(get("/" + shortCode)
                        .header("User-Agent", "JUnit Test Agent"))
                .andExpect(status().isFound());

        mockMvc.perform(get("/urls/" + shortCode + "/ip-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

}
