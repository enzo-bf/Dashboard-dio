CREATE TABLE IF NOT EXISTS cards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    column_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL,
    blocked TINYINT(1) NOT NULL DEFAULT 0,
    block_reason VARCHAR(500) NULL,
    unblock_reason VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cards_column
        FOREIGN KEY (column_id) REFERENCES board_columns (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cards_column_id ON cards (column_id);
CREATE INDEX idx_cards_blocked ON cards (blocked);
