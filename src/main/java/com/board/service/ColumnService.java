package com.board.service;

import com.board.configuration.TransactionExecutor;
import com.board.dto.BoardColumnDTO;
import com.board.dto.DtoMapper;
import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.BoardNotFoundException;
import com.board.exception.BusinessException;
import com.board.repository.BoardColumnRepository;
import com.board.repository.BoardRepository;

import java.util.Comparator;
import java.util.List;

public class ColumnService {

    private final TransactionExecutor transactionExecutor;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;

    public ColumnService(
            TransactionExecutor transactionExecutor,
            BoardRepository boardRepository,
            BoardColumnRepository boardColumnRepository
    ) {
        this.transactionExecutor = transactionExecutor;
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
    }

    public List<BoardColumnDTO> findByBoard(Long boardId) {
        return transactionExecutor.transactional(connection -> {
            ensureBoardExists(connection, boardId);
            return boardColumnRepository.findByBoardId(connection, boardId).stream()
                    .sorted(Comparator.comparing(BoardColumn::getOrder))
                    .map(DtoMapper::toColumnDto)
                    .toList();
        });
    }

    public BoardColumnDTO findInitial(Long boardId) {
        return findRequiredByType(boardId, ColumnType.INITIAL);
    }

    public BoardColumnDTO findCancel(Long boardId) {
        return findRequiredByType(boardId, ColumnType.CANCEL);
    }

    public List<BoardColumnDTO> workflowColumns(Long boardId) {
        return findByBoard(boardId).stream()
                .filter(column -> column.getType() != ColumnType.CANCEL)
                .sorted(Comparator.comparing(BoardColumnDTO::getOrder))
                .toList();
    }

    public BoardColumnDTO nextWorkflowColumn(Long boardId, Long currentColumnId) {
        List<BoardColumnDTO> workflow = workflowColumns(boardId);
        for (int index = 0; index < workflow.size(); index++) {
            BoardColumnDTO current = workflow.get(index);
            if (current.getId().equals(currentColumnId)) {
                if (index == workflow.size() - 1) {
                    throw new BusinessException("O card já está na coluna final do fluxo.");
                }
                return workflow.get(index + 1);
            }
        }
        throw new BusinessException("A coluna atual não pertence ao fluxo sequencial do board.");
    }

    private BoardColumnDTO findRequiredByType(Long boardId, ColumnType type) {
        return transactionExecutor.transactional(connection -> {
            ensureBoardExists(connection, boardId);
            return boardColumnRepository.findByBoardIdAndType(connection, boardId, type)
                    .map(DtoMapper::toColumnDto)
                    .orElseThrow(() -> new BusinessException(
                            "Coluna do tipo " + type + " não encontrada no board " + boardId
                    ));
        });
    }

    private void ensureBoardExists(java.sql.Connection connection, Long boardId) {
        boardRepository.findById(connection, boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }
}
