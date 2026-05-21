package com.example.movies.client.tickets;

import java.util.UUID;

public interface TicketsClient {
    boolean hasTicketsByMovieAndUser(UUID movieId, UUID userId);
}
