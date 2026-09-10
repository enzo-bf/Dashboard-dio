package com.board.controller;

import com.board.dto.BoardColumnDTO;
import com.board.dto.BoardDetailsDTO;
import com.board.dto.CardDTO;
import com.board.dto.CardDetailsDTO;
import com.board.exception.BusinessException;
import com.board.service.BoardService;
import com.board.service.CardService;
import com.board.service.ColumnService;
import com.board.util.ConsoleReader;
import com.board.util.DateTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoardMenuController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardMenuController.class);

    private final Long boardId;
    private final String boardName;
    private final BoardService boardService;
    private final ColumnService columnService;
    private final CardService cardService;
    private final ConsoleReader console;

    public BoardMenuController(
            Long boardId,
            String boardName,
            BoardService boardService,
            ColumnService columnService,
            CardService cardService,
            ConsoleReader console
    ) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.boardService = boardService;
        this.columnService = columnService;
        this.cardService = cardService;
        this.console = console;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int option = console.readInt("Escolha uma opção: ");
            try {
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCard();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> viewBoard();
                    case 7 -> viewCardById();
                    case 8 -> listCards();
                    case 9 -> running = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BusinessException exception) {
                System.out.println("Erro: " + exception.getMessage());
                LOGGER.warn("Regra de negócio: {}", exception.getMessage());
            } catch (Exception exception) {
                System.out.println("Erro inesperado: " + exception.getMessage());
                LOGGER.error("Falha no menu do board", exception);
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== MENU DO BOARD ==========");
        System.out.println("Board: " + boardId + " - " + boardName);
        System.out.println("1 - Criar card");
        System.out.println("2 - Mover card para próxima coluna");
        System.out.println("3 - Bloquear card");
        System.out.println("4 - Desbloquear card");
        System.out.println("5 - Cancelar card");
        System.out.println("6 - Visualizar board");
        System.out.println("7 - Visualizar card por ID");
        System.out.println("8 - Listar cards");
        System.out.println("9 - Voltar");
        System.out.println("===================================");
    }

    private void createCard() {
        String title = console.readLine("Título: ");
        String description = console.readLine("Descrição: ");
        CardDTO created = cardService.create(boardId, title, description);
        System.out.println("Card criado com id " + created.getId() + ".");
    }

    private void moveCard() {
        long cardId = console.readLong("Id do card: ");
        CardDTO moved = cardService.moveToNextColumn(boardId, cardId);
        System.out.println("Card " + moved.getId() + " movido para a coluna " + moved.getColumnId() + ".");
    }

    private void blockCard() {
        long cardId = console.readLong("Id do card: ");
        String reason = console.readLine("Motivo do bloqueio: ");
        cardService.block(boardId, cardId, reason);
        System.out.println("Card " + cardId + " bloqueado.");
    }

    private void unblockCard() {
        long cardId = console.readLong("Id do card: ");
        String reason = console.readLine("Motivo do desbloqueio: ");
        cardService.unblock(boardId, cardId, reason);
        System.out.println("Card " + cardId + " desbloqueado.");
    }

    private void cancelCard() {
        long cardId = console.readLong("Id do card: ");
        if (!console.confirm("Confirma o cancelamento do card " + cardId + "?")) {
            System.out.println("Cancelamento abortado.");
            return;
        }
        cardService.cancel(boardId, cardId);
        System.out.println("Card " + cardId + " cancelado.");
    }

    private void viewBoard() {
        BoardDetailsDTO details = boardService.findDetails(boardId);
        Map<Long, List<CardDTO>> cardsByColumn = details.getCards().stream()
                .collect(Collectors.groupingBy(CardDTO::getColumnId));

        System.out.println();
        System.out.println("Board: " + details.getId() + " - " + details.getName());
        for (BoardColumnDTO column : details.getColumns()) {
            System.out.println("--------------------------------------------------");
            System.out.println("[" + column.getOrder() + "] " + column.getName() + " (" + column.getType() + ")");
            List<CardDTO> cards = cardsByColumn.getOrDefault(column.getId(), List.of());
            if (cards.isEmpty()) {
                System.out.println("  (sem cards)");
            } else {
                cards.forEach(card -> System.out.println(
                        "  #" + card.getId()
                                + " " + card.getTitle()
                                + (card.isBlocked() ? " [BLOQUEADO]" : "")
                ));
            }
        }
        System.out.println("--------------------------------------------------");
    }

    private void viewCardById() {
        long cardId = console.readLong("Id do card: ");
        CardDetailsDTO card = cardService.findDetails(boardId, cardId);
        System.out.println("----- Detalhes do card -----");
        System.out.println("Id: " + card.getId());
        System.out.println("Título: " + card.getTitle());
        System.out.println("Descrição: " + emptyAsDash(card.getDescription()));
        System.out.println("Criado em: " + DateTimes.format(card.getCreatedAt()));
        System.out.println("Board: " + card.getBoardId() + " - " + card.getBoardName());
        System.out.println("Coluna: " + card.getColumnName() + " (" + card.getColumnType() + ")");
        System.out.println("Bloqueado: " + (card.isBlocked() ? "sim" : "não"));
        System.out.println("Motivo do bloqueio: " + emptyAsDash(card.getBlockReason()));
        System.out.println("Motivo do desbloqueio: " + emptyAsDash(card.getUnblockReason()));
    }

    private void listCards() {
        List<CardDTO> cards = cardService.listByBoard(boardId);
        if (cards.isEmpty()) {
            System.out.println("Nenhum card neste board.");
            return;
        }
        System.out.println("----- Cards -----");
        cards.forEach(card -> System.out.println(
                "#" + card.getId()
                        + " | " + card.getTitle()
                        + " | coluna " + card.getColumnId()
                        + (card.isBlocked() ? " | BLOQUEADO" : "")
        ));
    }

    private String emptyAsDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }
}
