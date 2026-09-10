package com.board.dto;

import com.board.entity.Board;
import com.board.entity.BoardColumn;
import com.board.entity.Card;

import java.util.List;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static BoardDTO toBoardDto(Board board) {
        return BoardDTO.builder()
                .id(board.getId())
                .name(board.getName())
                .build();
    }

    public static BoardColumnDTO toColumnDto(BoardColumn column) {
        return BoardColumnDTO.builder()
                .id(column.getId())
                .boardId(column.getBoardId())
                .name(column.getName())
                .order(column.getOrder())
                .type(column.getType())
                .build();
    }

    public static CardDTO toCardDto(Card card) {
        return CardDTO.builder()
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

    public static BoardDetailsDTO toBoardDetails(Board board, List<BoardColumn> columns, List<Card> cards) {
        return BoardDetailsDTO.builder()
                .id(board.getId())
                .name(board.getName())
                .columns(columns.stream().map(DtoMapper::toColumnDto).toList())
                .cards(cards.stream().map(DtoMapper::toCardDto).toList())
                .build();
    }

    public static CardDetailsDTO toCardDetails(Card card, BoardColumn column, Board board) {
        return CardDetailsDTO.builder()
                .id(card.getId())
                .boardId(board.getId())
                .boardName(board.getName())
                .columnId(column.getId())
                .columnName(column.getName())
                .columnOrder(column.getOrder())
                .columnType(column.getType())
                .title(card.getTitle())
                .description(card.getDescription())
                .createdAt(card.getCreatedAt())
                .blocked(card.isBlocked())
                .blockReason(card.getBlockReason())
                .unblockReason(card.getUnblockReason())
                .build();
    }
}
