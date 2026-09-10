package com.board;

import com.board.configuration.ApplicationProperties;
import com.board.configuration.ConnectionFactory;
import com.board.configuration.FlywayMigrator;
import com.board.controller.MainMenuController;
import com.board.repository.BoardColumnRepository;
import com.board.repository.BoardRepository;
import com.board.repository.CardRepository;
import com.board.repository.jdbc.JdbcBoardColumnRepository;
import com.board.repository.jdbc.JdbcBoardRepository;
import com.board.repository.jdbc.JdbcCardRepository;
import com.board.service.BoardService;
import com.board.service.CardService;
import com.board.service.ColumnService;
import com.board.util.ConsoleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {
        configureConsole();
        LOGGER.info("Iniciando Board Manager");

        ApplicationProperties properties = ApplicationProperties.load();
        FlywayMigrator.migrate(properties);

        ConnectionFactory connectionFactory = new ConnectionFactory(properties);
        BoardRepository boardRepository = new JdbcBoardRepository();
        BoardColumnRepository boardColumnRepository = new JdbcBoardColumnRepository();
        CardRepository cardRepository = new JdbcCardRepository();

        BoardService boardService = new BoardService(
                connectionFactory,
                boardRepository,
                boardColumnRepository,
                cardRepository
        );
        ColumnService columnService = new ColumnService(
                connectionFactory,
                boardRepository,
                boardColumnRepository
        );
        CardService cardService = new CardService(
                connectionFactory,
                boardRepository,
                boardColumnRepository,
                cardRepository
        );

        try (ConsoleReader console = new ConsoleReader()) {
            new MainMenuController(boardService, columnService, cardService, console).run();
        }
    }

    private static void configureConsole() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }
}
