package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.CollectionUtils;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    List<Comment> findAllByEvent_Id(Long eventId);

    List<Comment> findAllByAuthor_Id(Long authorId);

    default List<Comment> findWithFilters(List<Long> users,
                                          List<Long> events,
                                          List<Long> comments,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          String text,
                                          Pageable pageable) {
        var predicates = Specification.allOf(CommentRepository.Specs.after(rangeStart),
                CommentRepository.Specs.events(events),
                CommentRepository.Specs.before(rangeEnd),
                CommentRepository.Specs.comments(comments),
                CommentRepository.Specs.text(text),
                CommentRepository.Specs.users(users));
        return findAll(predicates, pageable).getContent();
    }


    class Specs {
        static Specification<Comment> users(List<Long> users) {
            return CollectionUtils.isEmpty(users) ? null :
                    (entity, query, cb)
                            -> entity.get("author").get("id").in(users);
        }

        static Specification<Comment> events(List<Long> events) {
            return CollectionUtils.isEmpty(events) ? null :
                    (entity, query, cb)
                            -> entity.get("event").get("id").in(events);
        }

        static Specification<Comment> comments(List<Long> comments) {
            return CollectionUtils.isEmpty(comments) ? null :
                    (entity, query, cb)
                            -> entity.get("id").in(comments);
        }

        static Specification<Comment> after(LocalDateTime rangeStart) {
            return rangeStart == null ? null :
                    (entity, query, cb)
                            -> cb.greaterThanOrEqualTo(entity.get("created"), rangeStart);
        }

        static Specification<Comment> before(LocalDateTime rangeEnd) {
            return rangeEnd == null ? null :
                    (entity, query, cb)
                            -> cb.lessThanOrEqualTo(entity.get("created"), rangeEnd);
        }

        static Specification<Comment> text(String text) {
            return text == null ? null :
                    (entity, query, cb)
                            -> cb.or(cb.like(cb.lower(entity.get("text")), "%" + text.toLowerCase() + "%"));
        }
    }
}
