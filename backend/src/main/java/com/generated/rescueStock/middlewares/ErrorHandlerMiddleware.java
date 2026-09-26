package com.generated.rescueStock.middlewares;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.services.ServiceException;

@RestControllerAdvice
public class ErrorHandlerMiddleware {
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException error) {
    return ResponseEntity.badRequest().body(body(error.getCode(), error.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception error) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(body(ErrorCodes.INTERNAL_ERROR, ErrorMessages.INTERNAL_ERROR));
  }

  private Map<String, Object> body(String code, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", code);
    body.put("message", message);
    return body;
  }
}
