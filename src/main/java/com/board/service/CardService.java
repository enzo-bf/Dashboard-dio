package com.board.service;

import com.board.configuration.TransactionExecutor;
import com.board.dto.CardDTO;
import com.board.dto.CardDetailsDTO;
import com.board.dto.DtoMapper;
import com.board.entity.Board;
import com.board.entity.BoardColumn;
import com.board.entity.Card;
import com.board.entity.ColumnType;
import com.board.exception.BoardNotFoundException;
import com.board.exception.BusinessException;
import com.board.exception.CardBlockedException;
import com.board.exception.CardNotFoundException;
import com.board.exception.InvalidColumnTransitionException;
import com.board.repository.BoardColumnRepository;
import com.board.repository.BoardRepository;
import com.board.repository.CardRepository;
import com.board.validation.CardValidator;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class CardService {

    private final TransactionExecutor transactionExecutor;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CardRepository cardRepository;

    public CardService(
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

    public CardDTO create(Long boardId, String title, String description) {
        CardValidator.validateCreate(title, description);
        return transactionExecutor.transactional(connection -> {
            ensureBoardExists(connection, boardId);
            BoardColumn initial = boardColumnRepository.findByBoardIdAndType(connection, boardId, ColumnType.INITIAL)
                    .orElseThrow(() -> new BusinessException("Coluna INITIAL não encontrada para o board " + boardId));
            Card card = Card.builder()
                    .columnId(initial.getId())
                    .title(title.trim())
                    .description(description == null ? null : description.trim())
                    .createdAt(LocalDateTime.now())
                    .blocked(false)
                    .build();
            return DtoMapper.toCardDto(cardRepository.save(connection, card));
        });
    }

    public CardDTO moveToNextColumn(Long boardId, Long cardId) {
        return transactionExecutor.transactional(connection -> {
            CardContext context = loadOwnedCard(connection, boardId, cardId);
            assertMovable(context.card(), context.column());
            if (context.column().getType() == ColumnType.FINAL) {
                throw new InvalidColumnTransitionException("Não é permitido mover um card que já está na coluna FINAL.");
            }
            BoardColumn next = nextWorkflowColumn(context.columns(), context.column());
            context.card().setColumnId(next.getId());
            cardRepository.update(connection, context.card());
            return DtoMapper.toCardDto(context.card());
        });
    }

    public CardDTO block(Long boardId, Long cardId, String reason) {
        CardValidator.validateBlockReason(reason);
        return transactionExecutor.transactional(connection -> {
            CardContext context = loadOwnedCard(connection, boardId, cardId);
            if (context.column().getType() == ColumnType.CANCEL) {
                throw new InvalidColumnTransitionException("Não é permitido bloquear um card cancelado.");
            }
            if (context.card().isBlocked()) {
                throw new BusinessException("O card " + cardId + " já está bloqueado.");
            }
            context.card().setBlocked(true);
            context.card().setBlockReason(reason.trim());
            cardRepository.update(connection, context.card());
            return DtoMapper.toCardDto(context.card());
        });
    }

    public CardDTO unblock(Long boardId, Long cardId, String reason) {
        CardValidator.validateUnblockReason(reason);
        return transactionExecutor.transactional(connection -> {
            CardContext context = loadOwnedCard(connection, boardId, cardId);
            if (!context.card().isBlocked()) {
                throw new BusinessException("O card " + cardId + " não está bloqueado.");
            }
            context.card().setBlocked(false);
            context.card().setUnblockReason(reason.trim());
            cardRepository.update(connection, context.card());
            return DtoMapper.toCardDto(context.card());
        });
    }

    public CardDTO cancel(Long boardId, Long cardId) {
        return transactionExecutor.transactional(connection -> {
            CardContext context = loadOwnedCard(connection, boardId, cardId);
            assertMovable(context.card(), context.column());
            if (context.column().getType() == ColumnType.FINAL) {
                throw new InvalidColumnTransitionException("Não é permitido cancelar um card que já está na coluna FINAL.");
            }
            BoardColumn cancelColumn = context.columns().stream()
                    .filter(column -> column.getType() == ColumnType.CANCEL)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Coluna CANCEL não encontrada para o board " + boardId));
            context.card().setColumnId(cancelColumn.getId());
            cardRepository.update(connection, context.card());
            return DtoMapper.toCardDto(context.card());
        });
    }

    public CardDetailsDTO findDetails(Long boardId, Long cardId) {
        return transactionExecutor.transactional(connection -> {
            CardContext context = loadOwnedCard(connection, boardId, cardId);
            Board board = boardRepository.findById(connection, boardId)
                    .orElseThrow(() -> new BoardNotFoundException(boardId));
            return DtoMapper.toCardDetails(context.card(), context.column(), board);
        });
    }

    public List<CardDTO> listByBoard(Long boardId) {
        return transactionExecutor.transactional(connection -> {
            ensureBoardExists(connection, boardId);
            return cardRepository.findByBoardId(connection, boardId).stream()
                    .map(DtoMapper::toCardDto)
                    .toList();
        });
    }

    private void assertMovable(Card card, BoardColumn currentColumn) {
        if (card.isBlocked()) {
            throw new CardBlockedException(card.getId());
        }
        if (currentColumn.getType() == ColumnType.CANCEL) {
            throw new InvalidColumnTransitionException("Não é permitido mover um card cancelado.");
        }
    }

    private BoardColumn nextWorkflowColumn(List<BoardColumn> columns, BoardColumn current) {
        List<BoardColumn> workflow = columns.stream()
                .filter(column -> column.getType() != ColumnType.CANCEL)
                .sorted(Comparator.comparing(BoardColumn::getOrder))
                .toList();
        for (int index = 0; index < workflow.size(); index++) {
            if (workflow.get(index).getId().equals(current.getId())) {
                if (index + 1 >= workflow.size()) {
                    throw new InvalidColumnTransitionException("Não existe próxima coluna no fluxo.");
                }
                BoardColumn next = workflow.get(index + 1);
                if (next.getOrder() <= current.getOrder()) {
                    throw new InvalidColumnTransitionException("A transição violaria a ordem das colunas.");
                }
                return next;
            }
        }
        throw new InvalidColumnTransitionException("A coluna atual não pertence ao fluxo sequencial do board.");
    }

    private CardContext loadOwnedCard(Connection connection, Long boardId, Long cardId) {
        ensureBoardExists(connection, boardId);
        Card card = cardRepository.findById(connection, cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        List<BoardColumn> columns = boardColumnRepository.findByBoardId(connection, boardId);
        BoardColumn current = columns.stream()
                .filter(column -> column.getId().equals(card.getColumnId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("O card " + cardId + " não pertence ao board " + boardId));
        return new CardContext(card, current, columns);
    }

    private void ensureBoardExists(Connection connection, Long boardId) {
        boardRepository.findById(connection, boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    private record CardContext(Card card, BoardColumn column, List<BoardColumn> columns) {
    }
}
