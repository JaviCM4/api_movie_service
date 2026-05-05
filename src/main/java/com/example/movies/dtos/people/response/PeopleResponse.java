package com.example.movies.dtos.people.response;

import com.example.movies.models.people.People;
import lombok.Value;

import java.util.UUID;

@Value
public class PeopleResponse {

    UUID id;
    String name;

    public static PeopleResponse from(People people) {
        return new PeopleResponse(people.getId(), people.getName());
    }
}
