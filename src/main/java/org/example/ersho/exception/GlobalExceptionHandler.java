package org.example.ersho.exception;

import org.example.ersho.entityDto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException e) {
        return Result.error(e.getMessage());
    }
}
