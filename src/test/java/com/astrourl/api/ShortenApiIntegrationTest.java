package com.astrourl.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AstrourlApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShortenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shortenReturnsFiveCharacterAlphanumericCode() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/path?q=1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", Matchers.matchesPattern("[0-9a-zA-Z]{5}")))
                .andExpect(jsonPath("$.shortUrl").value(Matchers.containsString("/")));
    }

    @Test
    void shortenAcceptsHttpLocalhost() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://localhost:8080/\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", Matchers.hasLength(5)));
    }

    @RepeatedTest(8)
    void repeatedShortenAlwaysProducesNonTrivialCode() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.org/x-" + System.nanoTime() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", Matchers.hasLength(5)))
                .andExpect(jsonPath("$.code", Matchers.not(Matchers.matchesPattern("^\\d$"))));
    }
}
