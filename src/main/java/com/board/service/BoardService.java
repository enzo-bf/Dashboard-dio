package com.board.service;

import com.board.configuration.TransactionExecutor;
import com.board.dto.BoardColumnDTO;
import com.board.dto.BoardDTO;
import com.board.dto.BoardDetailsDTO;
import com.board.dto.DtoMapper;
import com.board.entity.Board;
import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.BoardNotFoundException;
import com.board.exception.BusinessException;
import com.board.repository.BoardColumnRepository;
import com.board.repository.BoardRepository;
import com.board.repository.CardRepository;
import com.board.validation.BoardValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BoardService {

    private final TransactionExecutor transactionExecutor;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CardRepository cardRepository;

    public BoardService(
            TransactionExecutor transactionExecutor,
            BoardRepository boardRepository,
            BoardColumnRepository boardColumnRepository,
            CardRepository cardRepository
    ) {
        this.transactionExecutor = transactionExecutor;
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.cardRepository = cardRepository;
    }

    public BoardDTO create(String name, List<BoardColumn> columns) {
        BoardValidator.validateName(name);
        BoardValidator.validateColumns(columns);
        String normalizedName = name.trim();

        return transactionExecutor.transactional(connection -> {
            boardRepository.findByName(connection, normalizedName).ifPresent(existing -> {
                throw new BusinessException("Já existe um board com o nome: " + normalizedName);
            });

            Board saved = boardRepository.save(connection, Board.builder()
                    .name(normalizedName)
                    .createdAt(LocalDateTime.now())
                    .build());

            columns.forEach(column -> column.setBoardId(saved.getId()));
            boardColumnRepository.saveAll(connection, columns);
            return DtoMapper.toBoardDto(saved);
        });
    }

    public BoardDTO findById(Long id) {
        return transactionExecutor.transactional(connection ->
                boardRepository.findById(connection, id)
                        .map(DtoMapper::toBoardDto)
                        .orElseThrow(() -> new BoardNotFoundException(id))
        );
    }

    public BoardDetailsDTO findDetails(Long id) {
        return transactionExecutor.transactional(connection -> {
            Board board = boardRepository.findById(connection, id)
                    .orElseThrow(() -> new BoardNotFoundException(id));
            var columns = boardColumnRepository.findByBoardId(connection, id);
            var cards = cardRepository.findByBoardId(connection, id);
            return DtoMapper.toBoardDetails(board, columns, cards);
        });
    }

    public List<BoardDTO> findAll() {
        return transactionExecutor.transactional(connection ->
                boardRepository.findAll(connection).stream()
                        .map(DtoMapper::toBoardDto)
                        .toList()
        );
    }

    public void delete(Long id) {
        transactionExecutor.transactional(connection -> {
            boolean deleted = boardRepository.deleteById(connection, id);
            if (!deleted) {
                throw new BoardNotFoundException(id);
            }
            return true;
        });
    }

    public List<BoardColumnDTO> listColumns(Long boardId) {
        return transactionExecutor.transactional(connection -> {
            boardRepository.findById(connection, boardId)
                    .orElseThrow(() -> new BoardNotFoundException(boardId));
            return boardColumnRepository.findByBoardId(connection, boardId).stream()
                    .map(DtoMapper::toColumnDto)
                    .toList();
        });
    }

    public static List<BoardColumn> buildDefaultColumns(
            String initialName,
            List<String> pendingNames,
            String finalName,
            String cancelName
    ) {
        List<BoardColumn> columns = new ArrayList<>();
        int order = 1;
        columns.add(BoardColumn.builder()
                .name(initialName.trim())
                .order(order++)
                .type(ColumnType.INITIAL)
                .build());
        for (String pendingName : pendingNames) {
            columns.add(BoardColumn.builder()
                    .name(pendingName.trim())
                    .order(order++)
                    .type(ColumnType.PENDING)
                    .build());
        }
        columns.add(BoardColumn.builder()
                .name(finalName.trim())
                .order(order++)
                .type(ColumnType.FINAL)
                .build());
        columns.add(BoardColumn.builder()
                .name(cancelName.trim())
                .order(order)
                .type(ColumnType.CANCEL)
                .build());
        return columns;
    }
}
