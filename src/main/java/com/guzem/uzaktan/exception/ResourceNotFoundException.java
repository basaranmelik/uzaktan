package com.guzem.uzaktan.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s bulunamadı. %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
