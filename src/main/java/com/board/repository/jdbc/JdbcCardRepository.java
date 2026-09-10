package com.board.repository.jdbc;

import com.board.entity.Card;
import com.board.exception.PersistenceException;
import com.board.repository.CardRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCardRepository implements CardRepository {

    private static final String INSERT = """
            INSERT INTO cards (column_id, title, description, created_at, blocked, block_reason, unblock_reason)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE cards
            SET column_id = ?, title = ?, description = ?, blocked = ?, block_reason = ?, unblock_reason = ?
            WHERE id = ?
            """;
    private static final String FIND_BY_ID = """
            SELECT id, column_id, title, description, created_at, blocked, block_reason, unblock_reason
            FROM cards
            WHERE id = ?
            """;
    private static final String FIND_BY_COLUMN = """
            SELECT id, column_id, title, description, created_at, blocked, block_reason, unblock_reason
            FROM cards
            WHERE column_id = ?
            ORDER BY id
            """;
    private static final String FIND_BY_BOARD = """
            SELECT c.id, c.column_id, c.title, c.description, c.created_at, c.blocked, c.block_reason, c.unblock_reason
            FROM cards c
            INNER JOIN board_columns bc ON bc.id = c.column_id
            WHERE bc.board_id = ?
            ORDER BY bc.column_order, c.id
            """;

    @Override
    public Card save(Connection connection, Card card) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime createdAt = card.getCreatedAt() == null ? LocalDateTime.now() : card.getCreatedAt();
            statement.setLong(1, card.getColumnId());
            statement.setString(2, card.getTitle());
            statement.setString(3, card.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(createdAt));
            statement.setBoolean(5, card.isBlocked());
            setNullableString(statement, 6, card.getBlockReason());
            setNullableString(statement, 7, card.getUnblockReason());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new PersistenceException("Falha ao obter id gerado do card.", new IllegalStateException());
                }
                card.setId(keys.getLong(1));
                card.setCreatedAt(createdAt);
                return card;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao persistir card.", exception);
        }
    }

    @Override
    public void update(Connection connection, Card card) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setLong(1, card.getColumnId());
            statement.setString(2, card.getTitle());
            statement.setString(3, card.getDescription());
            statement.setBoolean(4, card.isBlocked());
            setNullableString(statement, 5, card.getBlockReason());
            setNullableString(statement, 6, card.getUnblockReason());
            statement.setLong(7, card.getId());
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new PersistenceException("Nenhum card atualizado para o id " + card.getId(), new IllegalStateException());
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao atualizar card.", exception);
        }
    }

    @Override
    public Optional<Card> findById(Connection connection, Long id) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao buscar card por id.", exception);
        }
    }

    @Override
    public List<Card> findByBoardId(Connection connection, Long boardId) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_BOARD)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Card> cards = new ArrayList<>();
                while (resultSet.next()) {
                    cards.add(map(resultSet));
                }
                return cards;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao listar cards do board.", exception);
        }
    }

    @Override
    public List<Card> findByColumnId(Connection connection, Long columnId) {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_COLUMN)) {
            statement.setLong(1, columnId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Card> cards = new ArrayList<>();
                while (resultSet.next()) {
                    cards.add(map(resultSet));
                }
                return cards;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Erro ao listar cards da coluna.", exception);
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private Card map(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return Card.builder()
                .id(resultSet.getLong("id"))
                .columnId(resultSet.getLong("column_id"))
                .title(resultSet.getString("title"))
                .description(resultSet.getString("description"))
                .createdAt(createdAt == null ? null : createdAt.toLocalDateTime())
                .blocked(resultSet.getBoolean("blocked"))
                .blockReason(resultSet.getString("block_reason"))
                .unblockReason(resultSet.getString("unblock_reason"))
                .build();
    }
}
