CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    amount DOUBLE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NULL
);

CREATE TABLE media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    object_name VARCHAR(500) NOT NULL,
    video_hash VARCHAR(255) NOT NULL,
    video_sig TEXT NOT NULL,
    sign_key_id VARCHAR(100) NOT NULL,
    sign_pk TEXT NOT NULL,

    CONSTRAINT fk_media_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE CASCADE
);