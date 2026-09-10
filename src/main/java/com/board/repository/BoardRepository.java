package com.board.repository;

import com.board.entity.Board;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    Board save(Connection connection, Board board);

    Optional<Board> findById(Connection connection, Long id);

    Optional<Board> findByName(Connection connection, String name);

    List<Board> findAll(Connection connection);

    boolean deleteById(Connection connection, Long id);
}
