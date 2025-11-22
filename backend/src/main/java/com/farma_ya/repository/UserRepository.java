package com.farma_ya.repository;

import com.farma_ya.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.rolId = :rolId")
    List<User> findByRole(@Param("rolId") Integer rolId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.rolId = :rolId")
    long countByRole(@Param("rolId") Integer rolId);

    void deleteByUsername(String username);
}
