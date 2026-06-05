// src/main/java/com/fleet_management/exception/InvalidStateTransitionException.java
package com.fleet_management.exception;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}