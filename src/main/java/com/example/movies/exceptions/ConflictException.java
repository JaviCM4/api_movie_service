package com.example.movies.exceptions;

/**
 * Exception that is thrown when there is a conflict with a resource (Example: Duplicate)
 */
public class ConflictException extends Exception {

    public ConflictException() {}

    public ConflictException(String message) {
        super(message);
    }
}
