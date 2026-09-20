package com.finance.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String title;
    private String category;
    private String content;
}
