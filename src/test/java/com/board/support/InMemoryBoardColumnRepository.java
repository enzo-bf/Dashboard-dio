package com.board.support;

import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;
import com.board.repository.BoardColumnRepository;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBoardColumnRepository implements BoardColumnRepository {

    private final Map<Long, BoardColumn> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public BoardColumn save(Connection connection, BoardColumn column) {
        long id = sequence.getAndIncrement();
        column.setId(id);
        storage.put(id, copy(column));
        return column;
    }

    @Override
    public void saveAll(Connection connection, List<BoardColumn> columns) {
        columns.forEach(column -> save(connection, column));
    }

    @Override
    public Optional<BoardColumn> findById(Connection connection, Long id) {
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public List<BoardColumn> findByBoardId(Connection connection, Long boardId) {
        return storage.values().stream()
                .filter(column -> column.getBoardId().equals(boardId))
                .sorted((left, right) -> Integer.compare(left.getOrder(), right.getOrder()))
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<BoardColumn> findByBoardIdAndType(Connection connection, Long boardId, ColumnType type) {
        return storage.values().stream()
                .filter(column -> column.getBoardId().equals(boardId) && column.getType() == type)
                .findFirst()
                .map(this::copy);
    }

    public void clear() {
        storage.clear();
        sequence.set(1);
    }

    private BoardColumn copy(BoardColumn column) {
        return BoardColumn.builder()
                .id(column.getId())
                .boardId(column.getBoardId())
                .name(column.getName())
                .order(column.getOrder())
                .type(column.getType())
                .build();
    }
}
