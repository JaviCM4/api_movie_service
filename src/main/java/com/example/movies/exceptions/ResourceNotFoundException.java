package com.example.movies.exceptions;

/**
 * Exception that is thrown when a resource is not found
 */
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException() {}

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
