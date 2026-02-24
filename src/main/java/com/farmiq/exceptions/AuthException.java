package com.farmiq.exceptions;

public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
    public AuthException(String message, Throwable cause) { super(message, cause); }
}
