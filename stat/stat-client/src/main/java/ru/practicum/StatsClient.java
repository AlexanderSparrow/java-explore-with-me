package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.ViewsStatsRequest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    private final RestClient restClient;

    public StatsClient(@Value("${stat-service.url}") String statsServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(statsServiceUrl)
                .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    public void sendHit(EndpointHitDto hit) {
        log.info("Отправка статистики: {}", hit);
        try {
            restClient.post()
                    .uri("/hit")
                    .body(hit)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка отправки статистики: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики: {}", e.getMessage(), e);
        }
    }

    public List<ViewStats> getStats(List<ViewsStatsRequest> requests) {
        List<ViewStats> allStats = new ArrayList<>();
        for (ViewsStatsRequest req : requests) {
            try {
                List<ViewStats> stats = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/stats")
                                .queryParam("start", req.getStart())
                                .queryParam("end", req.getEnd())
                                .queryParam("uris", req.getUri())
                                .queryParam("unique", req.isUnique())
                                .build())
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        });
                assert stats != null;
                allStats.addAll(stats);
            } catch (HttpStatusCodeException e) {
                log.error("Ошибка получения статистики: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("Ошибка при запросе статистики: {}", e.getMessage(), e);
            }
        }
        return allStats;
    }
}
