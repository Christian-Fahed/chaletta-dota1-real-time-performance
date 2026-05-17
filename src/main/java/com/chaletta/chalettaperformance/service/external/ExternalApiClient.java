package com.chaletta.chalettaperformance.service.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {

    private final RestTemplate restTemplate;


    @Value("${app.rgc.api-url}")
    private String apiUrl;

    @Value("${app.rgc.room}")
    private String room;

    @Value("${app.rgc.step}")
    private int step;

    /**
     * Fetch all games from the API URL endpoint as JSON data.
     * @return JsonNode containing all fetched games.
     * @throws Exception If an error occurred.
     */
    public JsonNode fetchGames(int page) throws Exception
    {
        // Build the HTTP headers.
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build the payload
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("room", room);
        payload.add("postData[page]", String.valueOf(page));
        payload.add("postData[orderby]", "0");
        payload.add("postData[order]", "DESC");
        payload.add("postData[step]", String.valueOf(step));
        payload.add("postData[search]", "A");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.getBody());
    }
}
