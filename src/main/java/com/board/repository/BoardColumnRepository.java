package com.board.repository;

import com.board.entity.BoardColumn;
import com.board.entity.ColumnType;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BoardColumnRepository {

    BoardColumn save(Connection connection, BoardColumn column);

    void saveAll(Connection connection, List<BoardColumn> columns);

    Optional<BoardColumn> findById(Connection connection, Long id);

    List<BoardColumn> findByBoardId(Connection connection, Long boardId);

    Optional<BoardColumn> findByBoardIdAndType(Connection connection, Long boardId, ColumnType type);
}
