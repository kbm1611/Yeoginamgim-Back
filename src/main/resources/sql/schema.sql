CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NULL,
    nickname VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(1000) NULL,
    provider VARCHAR(30) NOT NULL,
    provider_id VARCHAR(100) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS board (
    board_id BIGINT NOT NULL AUTO_INCREMENT,
    kakao_place_id VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (board_id),
    UNIQUE KEY uk_board_kakao_place_id (kakao_place_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trace (
    trace_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    trace_x INT NOT NULL,
    trace_y INT NOT NULL,
    trace_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (trace_id),
    KEY idx_trace_user_id (user_id),
    KEY idx_trace_board_id (board_id),
    KEY idx_trace_status_created_at (trace_status, created_at),
    CONSTRAINT fk_trace_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_trace_board FOREIGN KEY (board_id) REFERENCES board (board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trace_element (
    element_id BIGINT NOT NULL AUTO_INCREMENT,
    trace_id BIGINT NOT NULL,
    content_type VARCHAR(20) NOT NULL,
    text_content VARCHAR(500) NULL,
    image_url VARCHAR(500) NULL,
    element_x INT NULL,
    element_y INT NULL,
    style_json JSON NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (element_id),
    KEY idx_trace_element_trace_id (trace_id),
    CONSTRAINT fk_trace_element_trace FOREIGN KEY (trace_id) REFERENCES trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `like` (
    like_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    trace_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (like_id),
    UNIQUE KEY uk_like_user_trace (user_id, trace_id),
    KEY idx_like_trace_id (trace_id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_like_trace FOREIGN KEY (trace_id) REFERENCES trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report (
    report_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    trace_id BIGINT NOT NULL,
    report_kind VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_report_user_trace (user_id, trace_id),
    KEY idx_report_trace_id (trace_id),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_report_trace FOREIGN KEY (trace_id) REFERENCES trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
