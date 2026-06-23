package com.pbl.recycleai.advice;

import com.pbl.recycleai.exception.BadRequestException;
import com.pbl.recycleai.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad Request Test");
        ResponseEntity<Object> response = globalExceptionHandler.handleBadRequestException(ex);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Bad Request Test", response.getBody());
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource Not Found Test");
        ResponseEntity<Object> response = globalExceptionHandler.handleResourceNotFoundException(ex);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("Resource Not Found Test", response.getBody());
    }
}
