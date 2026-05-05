package com.example.movies.services.utils;

import com.example.movies.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResolverService {

    public <T> List<T> resolveEntities(List<UUID> ids, Function<List<UUID>, List<T>> fetcher, String entityName)
            throws ResourceNotFoundException {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<UUID> uniqueIds = ids.stream().distinct().toList();
        List<T> found = fetcher.apply(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more " + entityName + " do not exist");
        }
        return found;
    }

    public <R> List<R> deduplicateByKey(List<R> items, Function<R, UUID> keyExtractor) {
        if (items == null || items.isEmpty()) return Collections.emptyList();

        return new ArrayList<>(
            items.stream().collect(Collectors.toMap(
                    keyExtractor,
                    Function.identity(),
                    (first, second) -> first, LinkedHashMap::new))
            .values());
    }

    public <R> List<UUID> extractIds(List<R> requests, Function<R, UUID> idExtractor) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        return requests.stream().map(idExtractor).toList();
    }
}
