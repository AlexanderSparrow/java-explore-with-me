package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.RequestStatus;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequester(Long requesterId);

    boolean existsByRequesterAndEvent(Long requesterId, Long eventId);

    long countByEventAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByEvent(Long eventId);
}