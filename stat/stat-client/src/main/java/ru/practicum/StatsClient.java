package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class StatsClient {
    private final RestClient restClient;

    public StatsClient(@Value("${stat-service.url}") String statsServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(statsServiceUrl).build();
    }

    public void sendHit(EndpointHit hit) {
        restClient.post()
                .uri("/hit")
                .body(hit)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStats> getStats(String start, String end) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}

