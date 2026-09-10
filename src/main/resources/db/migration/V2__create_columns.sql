CREATE TABLE IF NOT EXISTS board_columns (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    column_order INT NOT NULL,
    type ENUM('INITIAL', 'PENDING', 'FINAL', 'CANCEL') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_board_columns_order (board_id, column_order),
    CONSTRAINT fk_board_columns_board
        FOREIGN KEY (board_id) REFERENCES boards (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_board_columns_board_id ON board_columns (board_id);
CREATE INDEX idx_board_columns_type ON board_columns (type);
