package com.churchmanagement.backend.repository;

import com.churchmanagement.backend.model.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {
    List<GalleryImage> findByIsHeroCarouselTrueOrderBySortOrderAsc();
    List<GalleryImage> findByIsPublicTrueOrderBySortOrderAsc();
}
