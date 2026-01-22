CREATE TABLE user_keys (
    id BIGINT auto_increment PRIMARY KEY ,
    user_id BIGINT NOT NULL,
    key_name VARCHAR(100) NOT NULL,
    public_key TEXT NOT NULL,
    encrypted_private_key TEXT NOT NULL,
    iv VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_user_key_name UNIQUE (user_id, key_name)
);

CREATE INDEX idx_user_keys_user_id ON user_keys(user_id);
CREATE INDEX idx_user_keys_status ON user_keys(status);