package com.danielvm.destiny2bot.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ProblemDetail handleIllegalStateException(IllegalStateException ise) {
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(ise.getMessage());
        return detail;
    }

    @ExceptionHandler
    public ProblemDetail handleWebClientException(WebClientResponseException wcre) {
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(wcre.getResponseBodyAsString());
        return detail;
    }

    @ExceptionHandler
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException cve) {
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(cve.getMessage());
        return detail;
    }

}
