package com.moustass.notification_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trigger_id", nullable = false)
    private Long triggerId;

    @Column(name = "received_id", nullable = false)
    private Long receivedId;

    @Column(name = "date_created_at")
    private LocalDateTime dateCreatedAt;

    @Column(name = "date_seen_at")
    private LocalDateTime dateSeenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_action", nullable = false)
    private Action action;

    public Notification(){}

    public Notification(Long triggerId, Long receivedId, String actionStr) {
        this.triggerId = triggerId;
        this.receivedId = receivedId;
        this.action = Action.valueOf(actionStr);
        this.setDateSeenAt(null);
        this.setDateCreatedAt(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public Long getReceivedId() {
        return receivedId;
    }

    public void setReceivedId(Long receivedId) {
        this.receivedId = receivedId;
    }

    public LocalDateTime getDateSeenAt() {
        return dateSeenAt;
    }

    public void setDateSeenAt(LocalDateTime dateSeenAt) {
        this.dateSeenAt = dateSeenAt;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public LocalDateTime getDateCreatedAt() {
        return dateCreatedAt;
    }

    public void setDateCreatedAt(LocalDateTime dateCreatedAt) {
        this.dateCreatedAt = dateCreatedAt;
    }
}
