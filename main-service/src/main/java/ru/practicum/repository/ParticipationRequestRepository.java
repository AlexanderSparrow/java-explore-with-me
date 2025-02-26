package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.id = :eventId AND pr.status = 'CONFIRMED'")
    int countConfirmedRequests(@Param("eventId") Long eventId);
}