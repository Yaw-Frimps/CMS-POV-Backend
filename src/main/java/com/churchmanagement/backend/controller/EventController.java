package com.churchmanagement.backend.controller;

import com.churchmanagement.backend.dto.EventDto;
import com.churchmanagement.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadEventImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(eventService.uploadImage(file));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        return ResponseEntity.ok(eventService.createEvent(eventDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        return ResponseEntity.ok(eventService.updateEvent(id, eventDto));
    }

    @PostMapping("/{eventId}/register/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or #memberId == principal?.memberProfile?.id")
    public ResponseEntity<EventDto> registerForEvent(@PathVariable Long eventId, @PathVariable Long memberId) {
        return ResponseEntity.ok(eventService.registerForEvent(eventId, memberId));
    }

    @DeleteMapping("/{eventId}/register/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or #memberId == principal?.memberProfile?.id")
    public ResponseEntity<EventDto> unregisterFromEvent(@PathVariable Long eventId, @PathVariable Long memberId) {
        return ResponseEntity.ok(eventService.unregisterFromEvent(eventId, memberId));
    }
}
