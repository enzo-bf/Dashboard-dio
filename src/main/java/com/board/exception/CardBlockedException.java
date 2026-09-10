package com.board.exception;

public class CardBlockedException extends BusinessException {

    public CardBlockedException(Long cardId) {
        super("O card " + cardId + " está bloqueado e não pode ser movido ou cancelado.");
    }
}
