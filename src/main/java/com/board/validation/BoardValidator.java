package com.board.validation;

import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.BusinessException;

import java.util.List;
import java.util.Objects;

public final class BoardValidator {

    private BoardValidator() {
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("O nome do board é obrigatório.");
        }
        if (name.trim().length() > 255) {
            throw new BusinessException("O nome do board deve ter no máximo 255 caracteres.");
        }
    }

    public static void validateColumns(List<BoardColumn> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new BusinessException("O board deve possuir colunas.");
        }

        long initial = count(columns, ColumnType.INITIAL);
        long pending = count(columns, ColumnType.PENDING);
        long finale = count(columns, ColumnType.FINAL);
        long cancel = count(columns, ColumnType.CANCEL);

        if (initial != 1) {
            throw new BusinessException("O board deve possuir exatamente uma coluna INITIAL.");
        }
        if (pending < 1) {
            throw new BusinessException("O board deve possuir ao menos uma coluna PENDING.");
        }
        if (finale != 1) {
            throw new BusinessException("O board deve possuir exatamente uma coluna FINAL.");
        }
        if (cancel != 1) {
            throw new BusinessException("O board deve possuir exatamente uma coluna CANCEL.");
        }

        columns.forEach(column -> {
            if (column.getName() == null || column.getName().isBlank()) {
                throw new BusinessException("Toda coluna deve possuir um nome.");
            }
            if (column.getOrder() == null || column.getOrder() < 1) {
                throw new BusinessException("Toda coluna deve possuir uma ordem válida.");
            }
            if (column.getType() == null) {
                throw new BusinessException("Toda coluna deve possuir um tipo.");
            }
        });

        long distinctOrders = columns.stream()
                .map(BoardColumn::getOrder)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (distinctOrders != columns.size()) {
            throw new BusinessException("A ordem das colunas não pode se repetir no mesmo board.");
        }

        List<BoardColumn> workflow = columns.stream()
                .filter(column -> column.getType() != ColumnType.CANCEL)
                .sorted((left, right) -> Integer.compare(left.getOrder(), right.getOrder()))
                .toList();

        if (workflow.getFirst().getType() != ColumnType.INITIAL) {
            throw new BusinessException("A primeira coluna do fluxo deve ser INITIAL.");
        }
        if (workflow.getLast().getType() != ColumnType.FINAL) {
            throw new BusinessException("A última coluna do fluxo deve ser FINAL.");
        }
        for (int index = 1; index < workflow.size() - 1; index++) {
            if (workflow.get(index).getType() != ColumnType.PENDING) {
                throw new BusinessException("Colunas intermediárias do fluxo devem ser do tipo PENDING.");
            }
        }
    }

    private static long count(List<BoardColumn> columns, ColumnType type) {
        return columns.stream().filter(column -> column.getType() == type).count();
    }
}
