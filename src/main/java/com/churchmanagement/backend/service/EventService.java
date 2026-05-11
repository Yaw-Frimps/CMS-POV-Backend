package com.churchmanagement.backend.service;

import com.churchmanagement.backend.dto.EventDto;
import com.churchmanagement.backend.model.Event;
import com.churchmanagement.backend.repository.EventRepository;
import com.churchmanagement.backend.repository.MemberRepository;
import com.churchmanagement.backend.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;

    public String uploadImage(MultipartFile file) {
        String id = java.util.UUID.randomUUID().toString();
        String storedFileName = fileStorageService.storeFile(file, "events", id, null);
        return fileStorageService.getFileUrl("events", id, storedFileName);
    }

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("null")
    public EventDto createEvent(EventDto eventDto) {
        Event event = Event.builder()
                .title(eventDto.getTitle())
                .description(eventDto.getDescription())
                .location(eventDto.getLocation())
                .imageUrl(eventDto.getImageUrl())
                .startTime(eventDto.getStartTime())
                .endTime(eventDto.getEndTime())
                .build();
        Event savedEvent = eventRepository.save(event);
        return mapToDto(savedEvent);
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Transactional
    @SuppressWarnings("null")
    public EventDto updateEvent(Long id, EventDto eventDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setLocation(eventDto.getLocation());
        event.setImageUrl(eventDto.getImageUrl());
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        Event savedEvent = eventRepository.save(event);
        return mapToDto(savedEvent);
    }

    @Transactional
    @SuppressWarnings("null")
    public EventDto registerForEvent(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!event.getRegisteredMembers().contains(member)) {
            event.getRegisteredMembers().add(member);
            eventRepository.save(event);
        }
        return mapToDto(event);
    }

    @Transactional
    @SuppressWarnings("null")
    public EventDto unregisterFromEvent(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (event.getRegisteredMembers().contains(member)) {
            event.getRegisteredMembers().remove(member);
            eventRepository.save(event);
        }
        return mapToDto(event);
    }

    private EventDto mapToDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .imageUrl(event.getImageUrl())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .registeredCount(event.getRegisteredMembers() != null ? event.getRegisteredMembers().size() : 0)
                .registeredMemberIds(event.getRegisteredMembers() != null ? event.getRegisteredMembers().stream().map(Member::getId).collect(Collectors.toList()) : java.util.Collections.emptyList())
                .build();
    }
}
