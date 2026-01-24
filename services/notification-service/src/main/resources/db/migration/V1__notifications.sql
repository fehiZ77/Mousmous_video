CREATE TABLE notifications(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trigger_id BIGINT NOT NULL,
    received_id BIGINT NOT NULL,
    date_created_at TIMESTAMP NULL,
    date_seen_at TIMESTAMP NULL,
    notif_action VARCHAR(50)
);