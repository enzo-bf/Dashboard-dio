package com.board.exception;

public class BoardNotFoundException extends BusinessException {

    public BoardNotFoundException(Long id) {
        super("Board não encontrado para o id: " + id);
    }
}
