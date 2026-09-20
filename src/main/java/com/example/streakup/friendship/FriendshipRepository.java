package com.example.streakup.friendship;

import com.example.streakup.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("""
            select f from Friendship f
            where (f.requester = :user or f.addressee = :user)
              and f.status = :status
            order by f.updatedAt desc
            """)
    List<Friendship> findForUser(@Param("user") User user, @Param("status") FriendshipStatus status);

    List<Friendship> findByAddresseeAndStatusOrderByCreatedAtDesc(User addressee, FriendshipStatus status);

    Optional<Friendship> findByRequesterAndAddressee(User requester, User addressee);

    @Query("""
            select f from Friendship f
            where ((f.requester = :first and f.addressee = :second)
                or (f.requester = :second and f.addressee = :first))
            """)
    Optional<Friendship> findBetween(@Param("first") User first, @Param("second") User second);
}
