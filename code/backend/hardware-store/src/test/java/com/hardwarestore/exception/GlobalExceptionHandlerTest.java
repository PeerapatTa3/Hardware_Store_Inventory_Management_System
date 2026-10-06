package com.hardwarestore.exception;

import com.hardwarestore.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void invalidPurchaseStateShouldReturnConflictResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/purchases/12");

        var response = handler.handleInvalidPurchaseState(
                new InvalidPurchaseStateException("Only pending purchases can be edited"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals("INVALID_PURCHASE_STATE", body.getError());
        assertEquals("/api/v1/purchases/12", body.getPath());
    }
}
