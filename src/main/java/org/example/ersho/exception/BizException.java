package org.example.ersho.exception;

/**
 * 业务异常：Service 层抛它，交给 GlobalExceptionHandler 统一翻译成 Result。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {

        super(message);
    }
}
