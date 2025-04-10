package ru.practicum.repository;

import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsById(@NonNull Long id);

    List<User> findByIdIn(List<Long> ids, Pageable pageable);

    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    List<User> findAllWithPagination(Pageable pageable);
}
