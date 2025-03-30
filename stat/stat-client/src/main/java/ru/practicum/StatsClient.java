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

import java.util.Collections;
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

    public List<ViewStats> getStats(String start, String end, List<String> uris) {
        log.info("Получение статистики с {} по {}, uris: {}", start, end, uris);
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("uris", uris != null ? uris : List.of())
                            //.queryParamIfPresent("uris", uris != null && !uris.isEmpty() ? Optional.of(uris) : Optional.empty())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (HttpStatusCodeException e) {
            log.error("Ошибка получения статистики: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при запросе статистики: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

   /* public ViewStats getStats(Long id) {
        List<Long> uris = new ArrayList<>();
        uris.add(id);
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("uris", uris)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }*/
}
