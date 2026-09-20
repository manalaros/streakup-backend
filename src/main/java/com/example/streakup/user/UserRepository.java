package com.example.streakup.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmail(String email);

    @Query("""
            select u from User u
            where u.id <> :currentId
              and (lower(u.username) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
            order by u.username
            """)
    List<User> searchByUsernameOrEmail(@Param("query") String query,
                                        @Param("currentId") Long currentId,
                                        Pageable pageable);
}
