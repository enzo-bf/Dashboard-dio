package com.board.service;

import com.board.dto.BoardDTO;
import com.board.dto.CardDTO;
import com.board.dto.CardDetailsDTO;
import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.BusinessException;
import com.board.exception.CardBlockedException;
import com.board.exception.CardNotFoundException;
import com.board.exception.InvalidColumnTransitionException;
import com.board.support.InMemoryBoardColumnRepository;
import com.board.support.InMemoryBoardRepository;
import com.board.support.InMemoryCardRepository;
import com.board.support.InMemoryTransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardServiceTest {

    private BoardService boardService;
    private CardService cardService;
    private Long boardId;
    private Long initialColumnId;
    private Long analysisColumnId;
    private Long developmentColumnId;
    private Long finalColumnId;
    private Long cancelColumnId;

    @BeforeEach
    void setUp() {
        InMemoryBoardRepository boardRepository = new InMemoryBoardRepository();
        InMemoryBoardColumnRepository columnRepository = new InMemoryBoardColumnRepository();
        InMemoryCardRepository cardRepository = new InMemoryCardRepository(columnRepository);
        InMemoryTransactionExecutor executor = new InMemoryTransactionExecutor();

        boardService = new BoardService(executor, boardRepository, columnRepository, cardRepository);
        cardService = new CardService(executor, boardRepository, columnRepository, cardRepository);

        List<BoardColumn> columns = BoardService.buildDefaultColumns(
                "Inicial",
                List.of("Análise", "Desenvolvimento"),
                "Final",
                "Cancelado"
        );
        BoardDTO board = boardService.create("Board Cards", columns);
        boardId = board.getId();
        var details = boardService.findDetails(boardId);
        initialColumnId = findColumn(details, ColumnType.INITIAL);
        analysisColumnId = details.getColumns().stream()
                .filter(column -> column.getType() == ColumnType.PENDING && column.getOrder() == 2)
                .findFirst()
                .orElseThrow()
                .getId();
        developmentColumnId = details.getColumns().stream()
                .filter(column -> column.getType() == ColumnType.PENDING && column.getOrder() == 3)
                .findFirst()
                .orElseThrow()
                .getId();
        finalColumnId = findColumn(details, ColumnType.FINAL);
        cancelColumnId = findColumn(details, ColumnType.CANCEL);
    }

    @Test
    void shouldCreateCardInInitialColumn() {
        CardDTO card = cardService.create(boardId, "Configurar ambiente", "Instalar JDK 21");
        assertEquals(initialColumnId, card.getColumnId());
        assertFalse(card.isBlocked());
    }

    @Test
    void shouldRejectCardWithoutTitle() {
        assertThrows(BusinessException.class, () -> cardService.create(boardId, " ", "desc"));
    }

    @Test
    void shouldMoveCardFollowingColumnOrder() {
        CardDTO card = cardService.create(boardId, "Implementar login", "Fluxo de autenticação");

        CardDTO afterFirstMove = cardService.moveToNextColumn(boardId, card.getId());
        assertEquals(analysisColumnId, afterFirstMove.getColumnId());

        CardDTO afterSecondMove = cardService.moveToNextColumn(boardId, card.getId());
        assertEquals(developmentColumnId, afterSecondMove.getColumnId());

        CardDTO afterThirdMove = cardService.moveToNextColumn(boardId, card.getId());
        assertEquals(finalColumnId, afterThirdMove.getColumnId());
    }

    @Test
    void shouldNotMoveBlockedCard() {
        CardDTO card = cardService.create(boardId, "Card bloqueado", "desc");
        cardService.block(boardId, card.getId(), "Aguardando informação");
        assertThrows(CardBlockedException.class, () -> cardService.moveToNextColumn(boardId, card.getId()));
    }

    @Test
    void shouldRequireBlockReason() {
        CardDTO card = cardService.create(boardId, "Card", "desc");
        assertThrows(BusinessException.class, () -> cardService.block(boardId, card.getId(), " "));
    }

    @Test
    void shouldRequireUnblockReason() {
        CardDTO card = cardService.create(boardId, "Card", "desc");
        cardService.block(boardId, card.getId(), "Dependência externa");
        assertThrows(BusinessException.class, () -> cardService.unblock(boardId, card.getId(), ""));
    }

    @Test
    void shouldUnblockCardWithReason() {
        CardDTO card = cardService.create(boardId, "Card", "desc");
        cardService.block(boardId, card.getId(), "Falta de spec");
        CardDTO unlocked = cardService.unblock(boardId, card.getId(), "Spec recebida");
        assertFalse(unlocked.isBlocked());
        assertEquals("Spec recebida", unlocked.getUnblockReason());
    }

    @Test
    void shouldCancelCardToCancelColumn() {
        CardDTO card = cardService.create(boardId, "Descartar", "desc");
        CardDTO cancelled = cardService.cancel(boardId, card.getId());
        assertEquals(cancelColumnId, cancelled.getColumnId());
    }

    @Test
    void shouldNotMoveCancelledCard() {
        CardDTO card = cardService.create(boardId, "Cancelar", "desc");
        cardService.cancel(boardId, card.getId());
        assertThrows(InvalidColumnTransitionException.class, () -> cardService.moveToNextColumn(boardId, card.getId()));
    }

    @Test
    void shouldNotMoveCardFromFinalColumn() {
        CardDTO card = cardService.create(boardId, "Finalizar", "desc");
        cardService.moveToNextColumn(boardId, card.getId());
        cardService.moveToNextColumn(boardId, card.getId());
        cardService.moveToNextColumn(boardId, card.getId());
        assertThrows(InvalidColumnTransitionException.class, () -> cardService.moveToNextColumn(boardId, card.getId()));
    }

    @Test
    void shouldNotCancelBlockedCard() {
        CardDTO card = cardService.create(boardId, "Bloqueado", "desc");
        cardService.block(boardId, card.getId(), "Impedimento");
        assertThrows(CardBlockedException.class, () -> cardService.cancel(boardId, card.getId()));
    }

    @Test
    void shouldReturnCardDetails() {
        CardDTO card = cardService.create(boardId, "Detalhe", "Texto");
        CardDetailsDTO details = cardService.findDetails(boardId, card.getId());
        assertEquals("Detalhe", details.getTitle());
        assertEquals("Board Cards", details.getBoardName());
        assertEquals(ColumnType.INITIAL, details.getColumnType());
        assertTrue(cardService.listByBoard(boardId).stream().anyMatch(item -> item.getId().equals(card.getId())));
    }

    @Test
    void shouldThrowWhenCardDoesNotExist() {
        assertThrows(CardNotFoundException.class, () -> cardService.findDetails(boardId, 999L));
    }

    private Long findColumn(com.board.dto.BoardDetailsDTO details, ColumnType type) {
        return details.getColumns().stream()
                .filter(column -> column.getType() == type)
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
