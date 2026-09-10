package com.board.exception;

public class PersistenceException extends BusinessException {

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
