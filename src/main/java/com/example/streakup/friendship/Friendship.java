package com.example.streakup.friendship;

import com.example.streakup.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(
        name = "uk_friendship_pair", columnNames = {"requester_id", "addressee_id"}))
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private FriendshipStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Friendship() {
    }

    public Friendship(User requester, User addressee) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void accept() {
        status = FriendshipStatus.ACCEPTED;
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getRequester() { return requester; }
    public User getAddressee() { return addressee; }
    public FriendshipStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
