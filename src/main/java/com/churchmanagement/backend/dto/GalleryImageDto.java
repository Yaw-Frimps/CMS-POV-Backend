package com.churchmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalleryImageDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private boolean isHeroCarousel;
    private boolean isPublic;
    private boolean isShowOnLanding;
    private int sortOrder;
    private LocalDateTime uploadedAt;
}
