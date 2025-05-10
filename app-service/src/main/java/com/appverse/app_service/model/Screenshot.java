package com.appverse.app_service.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screenshot {
    private String id; // Unique identifier for the screenshot
    private String imageUrl; 
    private String caption;
    private Integer order;
}