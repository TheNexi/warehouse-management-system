package warehouse.management.system.masi.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import warehouse.management.system.masi.response.ErrorResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionLayerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should expose API exception status and message")
    void shouldExposeApiExceptionStatusAndMessage() {
        ApiException exception = new ApiException(HttpStatus.BAD_REQUEST, "Invalid input");

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid input", exception.getMessage());
    }

    @Test
    @DisplayName("Should map API exception to response")
    void shouldMapApiExceptionToResponse() {
        ApiException exception = new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleApiException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().message());
    }

    @Test
    @DisplayName("Should map generic exception to internal server error")
    void shouldMapGenericExceptionToInternalServerError() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(new RuntimeException("x"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody().message());
    }
}
