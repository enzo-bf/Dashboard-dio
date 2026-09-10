package com.board.support;

import com.board.entity.Card;
import com.board.repository.CardRepository;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCardRepository implements CardRepository {

    private final Map<Long, Card> storage = new ConcurrentHashMap<>();
    private final InMemoryBoardColumnRepository columnRepository;
    private final AtomicLong sequence = new AtomicLong(1);

    public InMemoryCardRepository(InMemoryBoardColumnRepository columnRepository) {
        this.columnRepository = columnRepository;
    }

    @Override
    public Card save(Connection connection, Card card) {
        long id = sequence.getAndIncrement();
        card.setId(id);
        storage.put(id, copy(card));
        return card;
    }

    @Override
    public void update(Connection connection, Card card) {
        if (!storage.containsKey(card.getId())) {
            throw new IllegalStateException("Card inexistente: " + card.getId());
        }
        storage.put(card.getId(), copy(card));
    }

    @Override
    public Optional<Card> findById(Connection connection, Long id) {
        return Optional.ofNullable(storage.get(id)).map(this::copy);
    }

    @Override
    public List<Card> findByBoardId(Connection connection, Long boardId) {
        return storage.values().stream()
                .filter(card -> belongsToBoard(connection, card, boardId))
                .map(this::copy)
                .toList();
    }

    @Override
    public List<Card> findByColumnId(Connection connection, Long columnId) {
        return storage.values().stream()
                .filter(card -> card.getColumnId().equals(columnId))
                .map(this::copy)
                .toList();
    }

    public void clear() {
        storage.clear();
        sequence.set(1);
    }

    private boolean belongsToBoard(Connection connection, Card card, Long boardId) {
        return columnRepository.findById(connection, card.getColumnId())
                .map(column -> column.getBoardId().equals(boardId))
                .orElse(false);
    }

    private Card copy(Card card) {
        return Card.builder()
                .id(card.getId())
                .columnId(card.getColumnId())
                .title(card.getTitle())
                .description(card.getDescription())
                .createdAt(card.getCreatedAt())
                .blocked(card.isBlocked())
                .blockReason(card.getBlockReason())
                .unblockReason(card.getUnblockReason())
                .build();
    }
}
