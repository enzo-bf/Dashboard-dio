package com.board.controller;

import com.board.dto.BoardDTO;
import com.board.dto.BoardDetailsDTO;
import com.board.entity.BoardColumn;
import com.board.exception.BusinessException;
import com.board.service.BoardService;
import com.board.service.CardService;
import com.board.service.ColumnService;
import com.board.util.ConsoleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MainMenuController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuController.class);

    private final BoardService boardService;
    private final ColumnService columnService;
    private final CardService cardService;
    private final ConsoleReader console;

    public MainMenuController(
            BoardService boardService,
            ColumnService columnService,
            CardService cardService,
            ConsoleReader console
    ) {
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
                    case 1 -> createBoard();
                    case 2 -> selectBoard();
                    case 3 -> listBoards();
                    case 4 -> deleteBoard();
                    case 5 -> running = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BusinessException exception) {
                System.out.println("Erro: " + exception.getMessage());
                LOGGER.warn("Regra de negócio: {}", exception.getMessage());
            } catch (Exception exception) {
                System.out.println("Erro inesperado: " + exception.getMessage());
                LOGGER.error("Falha no menu principal", exception);
            }
        }
        System.out.println("Encerrando o sistema.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== MENU PRINCIPAL ==========");
        System.out.println("1 - Criar novo board");
        System.out.println("2 - Selecionar board");
        System.out.println("3 - Listar boards");
        System.out.println("4 - Excluir board");
        System.out.println("5 - Sair");
        System.out.println("====================================");
    }

    private void createBoard() {
        String name = console.readLine("Nome do board: ");
        String initialName = console.readLine("Nome da coluna inicial: ");
        int pendingCount = console.readInt("Quantidade de colunas pendentes (mínimo 1): ");
        if (pendingCount < 1) {
            throw new BusinessException("É necessário informar ao menos uma coluna pendente.");
        }
        List<String> pendingNames = new ArrayList<>();
        for (int index = 1; index <= pendingCount; index++) {
            pendingNames.add(console.readLine("Nome da coluna pendente " + index + ": "));
        }
        String finalName = console.readLine("Nome da coluna final: ");
        String cancelName = console.readLine("Nome da coluna de cancelamento: ");

        List<BoardColumn> columns = BoardService.buildDefaultColumns(initialName, pendingNames, finalName, cancelName);
        BoardDTO created = boardService.create(name, columns);
        System.out.println("Board criado com id " + created.getId() + " e nome \"" + created.getName() + "\".");
    }

    private void selectBoard() {
        listBoards();
        long boardId = console.readLong("Informe o id do board: ");
        BoardDetailsDTO details = boardService.findDetails(boardId);
        new BoardMenuController(details.getId(), details.getName(), boardService, columnService, cardService, console)
                .run();
    }

    private void listBoards() {
        List<BoardDTO> boards = boardService.findAll();
        if (boards.isEmpty()) {
            System.out.println("Nenhum board cadastrado.");
            return;
        }
        System.out.println("----- Boards -----");
        boards.forEach(board -> System.out.println(board.getId() + " - " + board.getName()));
    }

    private void deleteBoard() {
        listBoards();
        long boardId = console.readLong("Informe o id do board para excluir: ");
        if (!console.confirm("Confirma a exclusão do board " + boardId + "?")) {
            System.out.println("Exclusão cancelada.");
            return;
        }
        boardService.delete(boardId);
        System.out.println("Board " + boardId + " excluído.");
    }
}
