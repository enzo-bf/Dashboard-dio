package com.board.repository.jdbc;

import com.board.entity.Board;
import com.board.exception.PersistenceException;
import com.board.repository.BoardRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBoardRepository implements BoardRepository {

    private static final String INSERT = "INSERT INTO boards (name, created_at) VALUES (?, ?)";
    private static final String FIND_BY_ID = "SELECT id, name, created_at FROM boards WHERE id = ?";
    private static final String FIND_BY_NAME = "SELECT id, name, created_at FROM boards WHERE name = ?";
    private static final String FIND_ALL = "SELECT id, name, created_at FROM boards ORDER BY id";
    private static final String DELETE = "DELETE FROM boards WHERE id = ?";

    @Override
    public Board save(Connection connection, Board board) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime createdAt = board.getCreatedAt() == null ? LocalDateTime.now() : board.getCreatedAt();
            statement.setString(1, board.getName());
            statement.setTimestamp(2, Timestamp.valueOf(createdAt));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException("Falha ao obter id gerado do board.", new IllegalStateException());
                }
                board.setId(keys.getLong(1));
                board.setCreatedAt(createdAt);
                return board;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao persistir board.", exception);
        }
    }

    @Override
    public Optional<Board> findById(Connection connection, Long id) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao buscar board por id.", exception);
        }
    }

    @Override
    public Optional<Board> findByName(Connection connection, String name) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_NAME)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao buscar board por nome.", exception);
        }
    }

    @Override
    public List<Board> findAll(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Board> boards = new ArrayList<>();
            while (resultSet.next()) {
                boards.add(map(resultSet));
            }
            return boards;
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao listar boards.", exception);
        }
    }

    @Override
    public boolean deleteById(Connection connection, Long id) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao excluir board.", exception);
        }
    }

    private Board map(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return Board.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .createdAt(createdAt == null ? null : createdAt.toLocalDateTime())
                .build();
    }
}
