package com.example.movies.repositories.people;

import com.example.movies.models.people.People;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PeopleRepository extends JpaRepository<People, UUID> {

    Boolean existsByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
