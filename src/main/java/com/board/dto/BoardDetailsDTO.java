package com.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDetailsDTO {
    private Long id;
    private String name;
    @Builder.Default
    private List<BoardColumnDTO> columns = new ArrayList<>();
    @Builder.Default
    private List<CardDTO> cards = new ArrayList<>();
}
