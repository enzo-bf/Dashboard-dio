package com.board.exception;

public class CardNotFoundException extends BusinessException {

    public CardNotFoundException(Long id) {
        super("Card não encontrado para o id: " + id);
    }
}
