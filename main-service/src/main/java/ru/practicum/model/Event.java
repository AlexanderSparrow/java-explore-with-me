package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.Location;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false)
    private String annotation;

    @Embedded
    private Location location;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "participant_limit")
    private Integer participantLimit;
}
