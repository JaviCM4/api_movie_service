package com.example.movies.dtos.people.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdatePeopleRequest {

    @NotBlank
    @Size(min = 1, max = 255)
    String name;
}
