package com.danielvm.destiny2bot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ProblemDetail handleIllegalStateException(IllegalStateException ise) {
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(ise.getMessage());
        return detail;
    }
}
