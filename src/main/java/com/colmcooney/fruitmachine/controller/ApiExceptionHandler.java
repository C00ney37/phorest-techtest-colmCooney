package com.colmcooney.fruitmachine.controller;

import com.colmcooney.fruitmachine.domain.InvalidMachineConfigException;
import com.colmcooney.fruitmachine.service.MachineNotFoundException;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns failures into RFC 7807 problem responses. Spring's own request errors (malformed JSON, wrong method, and so
 * on) are already handled by the base class; this adds the game's own errors and a safe fallback.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MachineNotFoundException.class)
    public ProblemDetail handleMachineNotFound(MachineNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Machine not found");
        return problem;
    }

    /** For rules that span several fields, such as duplicate colours or a match length longer than the machine. */
    @ExceptionHandler(InvalidMachineConfigException.class)
    public ProblemDetail handleInvalidMachineConfig(InvalidMachineConfigException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid machine configuration");
        return problem;
    }

    /** Never shows the caller the cause of an unexpected failure; it is logged instead. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error handling request", exception);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal server error");
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> messagesByField = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                messagesByField.merge(fieldError.getField(), String.valueOf(fieldError.getDefaultMessage()), (first, second) -> first + "; " + second));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request has invalid fields");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", messagesByField);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }
}
