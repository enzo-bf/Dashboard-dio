package com.board.dto;

import com.board.entity.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailsDTO {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long columnId;
    private String columnName;
    private Integer columnOrder;
    private ColumnType columnType;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private boolean blocked;
    private String blockReason;
    private String unblockReason;
}
