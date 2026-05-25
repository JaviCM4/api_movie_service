package com.example.movies.dtos.people.request;

import com.example.movies.models.people.People;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreatePeopleRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres")
    String name;

    public People createEntity() {
        People people = new People();
        people.setName(name);
        return people;
    }
}
