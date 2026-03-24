package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.GalleryImageDto;
import com.churchmanagement.backend.model.GalleryImage;
import com.churchmanagement.backend.repository.GalleryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryImageRepository galleryImageRepository;
    private final FileStorageService fileStorageService;

    public List<GalleryImageDto> getPublicGallery() {
        return galleryImageRepository.findByIsPublicTrueOrderBySortOrderAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<GalleryImageDto> getLandingGallery() {
        // Return public images marked for landing, limited to 5 as requested
        return galleryImageRepository.findAll().stream()
                .filter(img -> img.isPublic() && img.isShowOnLanding())
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .limit(5)
                .map(this::mapToDto)
                .toList();
    }

    public List<GalleryImageDto> getHeroImages() {
        return galleryImageRepository.findByIsHeroCarouselTrueOrderBySortOrderAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<GalleryImageDto> getAllImages() {
        return galleryImageRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public GalleryImageDto uploadImage(MultipartFile file, String title, String description, boolean isPublic, boolean isHero, boolean isLanding, int sortOrder) {
        // Use a flat structure for gallery to simplify path resolution
        String storedFileName = fileStorageService.storeFile(file, "gallery", "", null);
        String fileUrl = "/uploads/gallery/" + storedFileName;

        GalleryImage image = GalleryImage.builder()
                .title(title)
                .description(description)
                .imageUrl(fileUrl)
                .isPublic(isPublic)
                .isHeroCarousel(isHero)
                .isShowOnLanding(isLanding)
                .sortOrder(sortOrder)
                .build();

        return mapToDto(galleryImageRepository.save(image));
    }

    @Transactional
    public GalleryImageDto updateImage(Long id, GalleryImageDto dto) {
        GalleryImage image = galleryImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        
        image.setTitle(dto.getTitle());
        image.setDescription(dto.getDescription());
        image.setPublic(dto.isPublic());
        image.setHeroCarousel(dto.isHeroCarousel());
        image.setShowOnLanding(dto.isShowOnLanding());
        image.setSortOrder(dto.getSortOrder());
        
        return mapToDto(galleryImageRepository.save(image));
    }

    @Transactional
    public void deleteImage(Long id) {
        galleryImageRepository.deleteById(id);
    }

    private GalleryImageDto mapToDto(GalleryImage image) {
        return GalleryImageDto.builder()
                .id(image.getId())
                .title(image.getTitle())
                .description(image.getDescription())
                .imageUrl(image.getImageUrl())
                .isPublic(image.isPublic())
                .isHeroCarousel(image.isHeroCarousel())
                .isShowOnLanding(image.isShowOnLanding())
                .sortOrder(image.getSortOrder())
                .uploadedAt(image.getUploadedAt())
                .build();
    }
}
