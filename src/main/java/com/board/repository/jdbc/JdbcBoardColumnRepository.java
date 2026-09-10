package com.board.repository.jdbc;

import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.exception.PersistenceException;
import com.board.repository.BoardColumnRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBoardColumnRepository implements BoardColumnRepository {

    private static final String INSERT = """
            INSERT INTO board_columns (board_id, name, column_order, type)
            VALUES (?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID = """
            SELECT id, board_id, name, column_order, type
            FROM board_columns
            WHERE id = ?
            """;
    private static final String FIND_BY_BOARD = """
            SELECT id, board_id, name, column_order, type
            FROM board_columns
            WHERE board_id = ?
            ORDER BY column_order
            """;
    private static final String FIND_BY_BOARD_AND_TYPE = """
            SELECT id, board_id, name, column_order, type
            FROM board_columns
            WHERE board_id = ? AND type = ?
            LIMIT 1
            """;

    @Override
    public BoardColumn save(Connection connection, BoardColumn column) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, column);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException("Falha ao obter id gerado da coluna.", new IllegalStateException());
                }
                column.setId(keys.getLong(1));
                return column;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao persistir coluna.", exception);
        }
    }

    @Override
    public void saveAll(Connection connection, List<BoardColumn> columns) {
        for (BoardColumn column : columns) {
            save(connection, column);
        }
    }

    @Override
    public Optional<BoardColumn> findById(Connection connection, Long id) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao buscar coluna por id.", exception);
        }
    }

    @Override
    public List<BoardColumn> findByBoardId(Connection connection, Long boardId) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_BOARD)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<BoardColumn> columns = new ArrayList<>();
                while (resultSet.next()) {
                    columns.add(map(resultSet));
                }
                return columns;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao listar colunas do board.", exception);
        }
    }

    @Override
    public Optional<BoardColumn> findByBoardIdAndType(Connection connection, Long boardId, ColumnType type) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_BOARD_AND_TYPE)) {
            statement.setLong(1, boardId);
            statement.setString(2, type.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao buscar coluna por tipo.", exception);
        }
    }

    private void bind(PreparedStatement statement, BoardColumn column) throws SQLException {
        statement.setLong(1, column.getBoardId());
        statement.setString(2, column.getName());
        statement.setInt(3, column.getOrder());
        statement.setString(4, column.getType().name());
    }

    private BoardColumn map(ResultSet resultSet) throws SQLException {
        return BoardColumn.builder()
                .id(resultSet.getLong("id"))
                .boardId(resultSet.getLong("board_id"))
                .name(resultSet.getString("name"))
                .order(resultSet.getInt("column_order"))
                .type(ColumnType.valueOf(resultSet.getString("type")))
                .build();
    }
}
