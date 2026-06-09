package com.yeginamgim.customboard.dto;

import lombok.Data;

@Data
public class CustomBoardCreateRequest {
    private String boardTitle;
    private String boardDescription;
    private String boardImageUrl;
}
