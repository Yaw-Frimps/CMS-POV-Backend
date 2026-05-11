package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.GalleryImageDto;
import com.churchmanagement.backend.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    // Public endpoints
    @GetMapping("/public")
    public ResponseEntity<List<GalleryImageDto>> getPublicGallery() {
        return ResponseEntity.ok(galleryService.getPublicGallery());
    }

    @GetMapping("/landing")
    public ResponseEntity<List<GalleryImageDto>> getLandingGallery() {
        return ResponseEntity.ok(galleryService.getLandingGallery());
    }

    @GetMapping("/hero")
    public ResponseEntity<List<GalleryImageDto>> getHeroImages() {
        return ResponseEntity.ok(galleryService.getHeroImages());
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GalleryImageDto>> getAllImages() {
        return ResponseEntity.ok(galleryService.getAllImages());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GalleryImageDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic,
            @RequestParam(value = "isHeroCarousel", defaultValue = "false") boolean isHero,
            @RequestParam(value = "isShowOnLanding", defaultValue = "false") boolean isLanding,
            @RequestParam(value = "sortOrder", defaultValue = "0") int sortOrder) {
        
        return ResponseEntity.ok(galleryService.uploadImage(file, title, description, isPublic, isHero, isLanding, sortOrder));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GalleryImageDto> updateImage(@PathVariable Long id, @RequestBody GalleryImageDto dto) {
        return ResponseEntity.ok(galleryService.updateImage(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        galleryService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
