package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(@Param("ids") List<Long> ids, int from, int size);

    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    List<User> findAllWithPagination(int from, int size);
}
