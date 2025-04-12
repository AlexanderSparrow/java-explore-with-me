package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.practicum.enums.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.state = 'PUBLISHED'")
    Optional<Event> findPublishedEventById(@Param("eventId") Long eventId);

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    boolean existsByCategory_Id(Long catId);

    default List<Event> findWithFilters(List<Long> users,
                                        List<EventState> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Boolean paid,
                                        String text,
                                        Pageable pageable) {
        var predicates = Specification.allOf(Specs.after(rangeStart),
                Specs.states(states),
                Specs.before(rangeEnd),
                Specs.categories(categories),
                Specs.paid(paid),
                Specs.text(text),
                Specs.users(users));
        return findAll(predicates, pageable).getContent();
    }

    class Specs {
        static Specification<Event> users(List<Long> users) {
            return CollectionUtils.isEmpty(users) ? null :
                    (entity, query, cb)
                            -> entity.get("initiator").get("id").in(users);
        }

        static Specification<Event> states(List<EventState> states) {
            return CollectionUtils.isEmpty(states) ? null :
                    (entity, query, cb)
                            -> entity.get("state").in(states);
        }

        static Specification<Event> categories(List<Long> categories) {
            return CollectionUtils.isEmpty(categories) ? null :
                    (entity, query, cb)
                            -> entity.get("category").get("id").in(categories);
        }

        static Specification<Event> after(LocalDateTime rangeStart) {
            return rangeStart == null ? null :
                    (entity, query, cb)
                            -> cb.greaterThanOrEqualTo(entity.get("eventDate"), rangeStart);
        }

        static Specification<Event> before(LocalDateTime rangeEnd) {
            return rangeEnd == null ? null :
                    (entity, query, cb)
                            -> cb.lessThanOrEqualTo(entity.get("eventDate"), rangeEnd);
        }

        static Specification<Event> text(String text) {
            return text == null ? null :
                    (entity, query, cb)
                            -> cb.or(cb.like(cb.lower(entity.get("annotation")), "%" + text.toLowerCase() + "%"),
                            cb.like(cb.lower(entity.get("description")), "%" + text + "%"));
        }

        static Specification<Event> paid(Boolean paid) {
            return paid == null ? null :
                    (entity, query, cb)
                            -> cb.equal(entity.get("paid"), paid);
        }
    }
}
