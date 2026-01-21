CREATE TABLE notifications(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trigger_id BIGINT NOT NULL,
    received_id BIGINT NOT NULL,
    date_seen_at TIMESTAMP NULL,
    action ENUM(
        'TRANSACTION_CREATED',
        'TRANSACTION_VERIFIED_OK',
        'TRANSACTION_VERIFIED_NOK'
    ) NOT NULL,
    transaction_id BIGINT NOT NULL
);