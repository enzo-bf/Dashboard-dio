package com.board.validation;

import com.board.exception.BusinessException;

public final class CardValidator {

    private CardValidator() {
    }

    public static void validateCreate(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("O título do card é obrigatório.");
        }
        if (title.trim().length() > 255) {
            throw new BusinessException("O título do card deve ter no máximo 255 caracteres.");
        }
        if (description != null && description.length() > 4000) {
            throw new BusinessException("A descrição do card deve ter no máximo 4000 caracteres.");
        }
    }

    public static void validateBlockReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("O motivo do bloqueio é obrigatório.");
        }
        if (reason.trim().length() > 500) {
            throw new BusinessException("O motivo do bloqueio deve ter no máximo 500 caracteres.");
        }
    }

    public static void validateUnblockReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("O motivo do desbloqueio é obrigatório.");
        }
        if (reason.trim().length() > 500) {
            throw new BusinessException("O motivo do desbloqueio deve ter no máximo 500 caracteres.");
        }
    }
}
