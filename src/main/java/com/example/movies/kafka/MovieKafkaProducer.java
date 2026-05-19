package com.example.movies.kafka;


import com.example.movies.events.CreatedMovieEvent;
import com.example.movies.events.UpdatedMovieEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MovieKafkaProducer {

    private final String TOPIC_CREATED = "movies.movie.created";
    private final String TOPIC_UPDATED = "movies.movie.updated";
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public MovieKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCreatedMovieEvent(CreatedMovieEvent event) {
        kafkaTemplate.send(TOPIC_CREATED, event);
    }

    public void sendUpdatedMovieEvent(UpdatedMovieEvent event) {
        kafkaTemplate.send(TOPIC_UPDATED, event);
    }
}
