package com.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Long id;
    private Long columnId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private boolean blocked;
    private String blockReason;
    private String unblockReason;
}
