package com.board.service;

import com.board.dto.BoardDTO;
import com.board.dto.BoardDetailsDTO;
import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.BoardNotFoundException;
import com.board.exception.BusinessException;
import com.board.support.InMemoryBoardColumnRepository;
import com.board.support.InMemoryBoardRepository;
import com.board.support.InMemoryCardRepository;
import com.board.support.InMemoryTransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardServiceTest {

    private InMemoryBoardRepository boardRepository;
    private InMemoryBoardColumnRepository columnRepository;
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardRepository = new InMemoryBoardRepository();
        columnRepository = new InMemoryBoardColumnRepository();
        InMemoryCardRepository cardRepository = new InMemoryCardRepository(columnRepository);
        boardService = new BoardService(
                new InMemoryTransactionExecutor(),
                boardRepository,
                columnRepository,
                cardRepository
        );
    }

    @Test
    void shouldCreateBoardWithRequiredColumns() {
        BoardDTO created = boardService.create("Projeto Alpha", validColumns());

        assertEquals(1L, created.getId());
        assertEquals("Projeto Alpha", created.getName());
        BoardDetailsDTO details = boardService.findDetails(created.getId());
        assertEquals(4, details.getColumns().size());
        assertEquals(ColumnType.INITIAL, details.getColumns().get(0).getType());
        assertEquals(ColumnType.PENDING, details.getColumns().get(1).getType());
        assertEquals(ColumnType.FINAL, details.getColumns().get(2).getType());
        assertEquals(ColumnType.CANCEL, details.getColumns().get(3).getType());
    }

    @Test
    void shouldAllowMultiplePendingColumns() {
        List<BoardColumn> columns = BoardService.buildDefaultColumns(
                "Backlog",
                List.of("Análise", "Desenvolvimento", "Homologação"),
                "Concluído",
                "Cancelado"
        );

        BoardDTO created = boardService.create("Kanban Dev", columns);
        BoardDetailsDTO details = boardService.findDetails(created.getId());

        long pendingCount = details.getColumns().stream()
                .filter(column -> column.getType() == ColumnType.PENDING)
                .count();
        assertEquals(3, pendingCount);
        assertEquals(6, details.getColumns().size());
    }

    @Test
    void shouldRejectBoardWithoutName() {
        assertThrows(BusinessException.class, () -> boardService.create("  ", validColumns()));
    }

    @Test
    void shouldRejectBoardWithoutInitialColumn() {
        List<BoardColumn> columns = validColumns();
        columns.getFirst().setType(ColumnType.PENDING);
        assertThrows(BusinessException.class, () -> boardService.create("Inválido", columns));
    }

    @Test
    void shouldRejectDuplicatedBoardName() {
        boardService.create("Duplicado", validColumns());
        assertThrows(BusinessException.class, () -> boardService.create("Duplicado", validColumns()));
    }

    @Test
    void shouldListCreatedBoards() {
        boardService.create("Board A", validColumns());
        boardService.create("Board B", validColumns());
        assertEquals(2, boardService.findAll().size());
    }

    @Test
    void shouldThrowWhenBoardDoesNotExist() {
        assertThrows(BoardNotFoundException.class, () -> boardService.findById(99L));
        assertThrows(BoardNotFoundException.class, () -> boardService.delete(99L));
    }

    @Test
    void shouldDeleteExistingBoard() {
        BoardDTO created = boardService.create("Remover", validColumns());
        boardService.delete(created.getId());
        assertTrue(boardService.findAll().isEmpty());
    }

    private List<BoardColumn> validColumns() {
        return new ArrayList<>(BoardService.buildDefaultColumns(
                "Inicial",
                List.of("Pendente"),
                "Final",
                "Cancelar"
        ));
    }
}
