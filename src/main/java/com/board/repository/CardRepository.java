package com.board.repository;

import com.board.entity.Card;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CardRepository {

    Card save(Connection connection, Card card);

    void update(Connection connection, Card card);

    Optional<Card> findById(Connection connection, Long id);

    List<Card> findByBoardId(Connection connection, Long boardId);

    List<Card> findByColumnId(Connection connection, Long columnId);
}
