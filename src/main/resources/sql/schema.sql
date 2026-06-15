CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NULL,
    nickname VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(1000) NULL,
    birth_date VARCHAR(6) NULL,
    provider VARCHAR(30) NOT NULL,
    provider_id VARCHAR(100) NULL,
    deleted_at DATETIME(6) NULL,
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

CREATE TABLE IF NOT EXISTS custom_board (
    custom_board_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    board_title VARCHAR(100) NOT NULL,
    board_description VARCHAR(500) NULL,
    board_image_url VARCHAR(500) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (custom_board_id),
    KEY idx_custom_board_user_id (user_id),
    CONSTRAINT fk_custom_board_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS custom_board_member (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    custom_board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_custom_board_member_board_user (custom_board_id, user_id),
    KEY idx_custom_board_member_user_id (user_id),
    CONSTRAINT fk_custom_board_member_board FOREIGN KEY (custom_board_id) REFERENCES custom_board (custom_board_id),
    CONSTRAINT fk_custom_board_member_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS custom_board_invite (
    invite_id BIGINT NOT NULL AUTO_INCREMENT,
    custom_board_id BIGINT NOT NULL,
    invite_code VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    expired_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (invite_id),
    UNIQUE KEY uk_custom_board_invite_code (invite_code),
    KEY idx_custom_board_invite_board_id (custom_board_id),
    KEY idx_custom_board_invite_user_id (user_id),
    CONSTRAINT fk_custom_board_invite_board FOREIGN KEY (custom_board_id) REFERENCES custom_board (custom_board_id),
    CONSTRAINT fk_custom_board_invite_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favorite_place (
    favorite_place_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    kakao_place_id VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (favorite_place_id),
    UNIQUE KEY uk_favorite_place_user_place (user_id, kakao_place_id),
    KEY idx_favorite_place_user_id (user_id),
    KEY idx_favorite_place_kakao_place_id (kakao_place_id),
    CONSTRAINT fk_favorite_place_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trace (
    trace_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    board_id BIGINT NULL,
    custom_board_id BIGINT NULL,
    trace_x INT NOT NULL,
    trace_y INT NOT NULL,
    trace_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (trace_id),
    KEY idx_trace_user_id (user_id),
    KEY idx_trace_board_id (board_id),
    KEY idx_trace_custom_board_id (custom_board_id),
    KEY idx_trace_status_created_at (trace_status, created_at),
    CONSTRAINT fk_trace_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_trace_board FOREIGN KEY (board_id) REFERENCES board (board_id),
    CONSTRAINT fk_trace_custom_board FOREIGN KEY (custom_board_id) REFERENCES custom_board (custom_board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE trace MODIFY COLUMN board_id BIGINT NULL;

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

CREATE TABLE IF NOT EXISTS user_follow (
    follow_id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (follow_id),
    UNIQUE KEY uk_user_follow_follower_following (follower_id, following_id),
    KEY idx_user_follow_follower_id (follower_id),
    KEY idx_user_follow_following_id (following_id),
    CONSTRAINT fk_user_follow_follower FOREIGN KEY (follower_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_follow_following FOREIGN KEY (following_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    receiver_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    trace_id BIGINT NULL,
    notification_type VARCHAR(50) NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BIT NOT NULL DEFAULT 0,
    read_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (notification_id),
    KEY idx_notification_receiver_read_created (receiver_id, is_read, created_at),
    KEY idx_notification_receiver_created (receiver_id, created_at),
    KEY idx_notification_trace_id (trace_id),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users (user_id),
    CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES users (user_id),
    CONSTRAINT fk_notification_trace FOREIGN KEY (trace_id) REFERENCES trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
