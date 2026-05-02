package com.example.movies.models.classification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.movies.models.country.Country;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "classification")
@Data
@NoArgsConstructor
public class Classification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonIgnore
    private Country country;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "age_limit", nullable = false)
    private Integer ageLimit;
}
