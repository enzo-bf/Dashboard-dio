package com.board.support;

import com.board.entity.Board;
import com.board.repository.BoardRepository;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryBoardRepository implements BoardRepository {

    private final Map<Long, Board> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Board save(Connection connection, Board board) {
        long id = sequence.getAndIncrement();
        board.setId(id);
        storage.put(id, copy(board));
        return board;
    }

    @Override
    public Optional<Board> findById(Connection connection, Long id) {
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public Optional<Board> findByName(Connection connection, String name) {
        return storage.values().stream()
                .filter(board -> board.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<Board> findAll(Connection connection) {
        return storage.values().stream()
                .map(this::copy)
                .toList();
    }

    @Override
    public boolean deleteById(Connection connection, Long id) {
        return storage.remove(id) != null;
    }

    public void clear() {
        storage.clear();
        sequence.set(1);
    }

    public List<Board> snapshot() {
        return new ArrayList<>(storage.values());
    }

    private Board copy(Board board) {
        return Board.builder()
                .id(board.getId())
                .name(board.getName())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
