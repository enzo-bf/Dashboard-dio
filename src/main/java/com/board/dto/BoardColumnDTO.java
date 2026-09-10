package com.board.dto;

import com.board.entity.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnDTO {
    private Long id;
    private Long boardId;
    private String name;
    private Integer order;
    private ColumnType type;
}
