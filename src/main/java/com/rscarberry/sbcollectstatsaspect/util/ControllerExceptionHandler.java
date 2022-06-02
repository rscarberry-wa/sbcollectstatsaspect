package com.rscarberry.sbcollectstatsaspect.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;


@RestControllerAdvice
@Slf4j
class ControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)  
    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody HttpErrorInfo handleIllegalArgumentExceptions(
    ServerHttpRequest request, IllegalArgumentException ex) {
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }  

    private HttpErrorInfo createHttpErrorInfo(
         HttpStatus httpStatus,
         ServerHttpRequest request,
         Exception ex
     ) {
         final String path = request.getPath().pathWithinApplication().value();
         final String message = ex.getMessage();
         return new HttpErrorInfo(httpStatus, path, message);
     }
}