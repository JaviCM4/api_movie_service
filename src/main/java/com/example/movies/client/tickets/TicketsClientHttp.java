package com.example.movies.client.tickets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TicketsClientHttp implements TicketsClient {
    private final RestClient restClient;

    @Value("${services.tickets.url}")
    private String ticketsServiceUrl;

    public TicketsClientHttp(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public boolean hasTicketsByMovieAndUser(java.util.UUID movieId, java.util.UUID userId) {
        Boolean result = restClient.get()
                .uri(ticketsServiceUrl + "/tickets/internal/has-tickets/movie/{movieId}/user?userId={userId}", movieId, userId)
                .retrieve()
                .body(Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}
