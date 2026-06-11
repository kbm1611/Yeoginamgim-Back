-- users 샘플
-- 샘플 계정 비밀번호: 12341234
INSERT IGNORE INTO users (user_id, email, password, nickname, birth_date, provider, provider_id, created_at, updated_at) VALUES
(1, 'example1@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '새벽달', '990101', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(2, 'qwe1@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '은하수', '060615', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(3, 'qwe12@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '달빛별', '030228', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(4, 'qwe123@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '푸른밤', '940229', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(5, 'qwe1234@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '여름비', NULL, 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(6, 'qwe12345@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '잔잔한바다', '041231', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(7, 'qwer1@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '하늘조각', '000101', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(8, 'qwer12@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '바람꽃', '080229', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(9, 'qwer123@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '치즈냥', NULL, 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(10, 'qwer1234@sample.com', '$2a$10$hiJwEie4CO9EaLmg0PW44ud9Dh3M6zcd8m.RrMGCAsMyqAqX1Q.Ga', '솜구름', '991231', 'LOCAL', NULL, '2026-05-28 14:32:18', '2026-06-02 14:32:18');

-- board 샘플
INSERT IGNORE INTO board (board_id, kakao_place_id, created_at) VALUES
(1, '26338954', '2026-05-28 14:32:18'),
(2, '26338953', '2026-05-28 14:32:18'),
(3, '73753737', '2026-05-28 14:32:18'),
(4, '87778373', '2026-05-28 14:32:18'),
(5, '75373753', '2026-05-28 14:32:18'),
(6, '57753735', '2026-05-28 14:32:18'),
(7, '86533235', '2026-05-28 14:32:18'),
(8, '27247972', '2026-05-28 14:32:18'),
(9, '22787687', '2026-05-28 14:32:18'),
(10, '972437375', '2026-05-28 14:32:18');

-- trace 샘플
INSERT IGNORE INTO trace (trace_id, user_id, board_id, trace_x, trace_y, trace_status, created_at, updated_at) VALUES
(1, 1, 3, 30, 50, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(2, 1, 2, 25, 87, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(3, 2, 1, 73, 54, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(4, 2, 3, 33, 65, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(5, 2, 2, 86, 24, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(6, 3, 1, 63, 64, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(7, 4, 4, 12, 31, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(8, 5, 4, 14, 65, 'ACTIVE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(9, 6, 5, 65, 43, 'HIDE', '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(10, 7, 6, 87, 23, 'HIDE', '2026-05-28 14:32:18', '2026-06-02 14:32:18');

-- trace_element 샘플
INSERT IGNORE INTO trace_element (element_id, trace_id, content_type, text_content, image_url, element_x, element_y, style_json, created_at, updated_at) VALUES
(1, 1, 'POST_IT', '오늘의 순간', 'C:/sample/uploads/image01.jpg', 30, 50, JSON_OBJECT('backgroundColor', '#E6E6FA', 'textColor', '#FFFF00', 'fontFamily', 'Arial'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(2, 2, 'POST_IT', '너와 함께한 하루', 'C:/sample/uploads/image02.png', 80, 50, JSON_OBJECT('backgroundColor', '#FFF8DC', 'textColor', '#333333', 'fontFamily', 'Georgia'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(3, 3, 'POST_IT', '우리의 여름', 'C:/sample/uploads/image03.jpg', 10, 67, JSON_OBJECT('backgroundColor', '#DFF7E2', 'textColor', '#1F4D2B', 'fontFamily', 'Verdana'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(4, 4, 'POST_IT', '기억하고 싶은 밤', 'C:/sample/uploads/image04.png', 50, 52, JSON_OBJECT('backgroundColor', '#FFE4E1', 'textColor', '#7A1E1E', 'fontFamily', 'Tahoma'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(5, 5, 'POST_IT', '행복했던 시간', 'C:/sample/uploads/image05.jpg', 76, 24, JSON_OBJECT('backgroundColor', '#E0FFFF', 'textColor', '#005B5B', 'fontFamily', 'Trebuchet MS'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(6, 6, 'POLAROID', '소중한 추억', 'C:/sample/uploads/image06.png', 76, 54, JSON_OBJECT('backgroundColor', '#FFFFFF', 'textColor', '#222222', 'fontFamily', 'Courier New'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(7, 7, 'POLAROID', '다시 오고 싶은 곳', 'C:/sample/uploads/image07.jpg', 70, 57, JSON_OBJECT('backgroundColor', '#F5F5DC', 'textColor', '#5C4033', 'fontFamily', 'Times New Roman'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(8, 8, 'POLAROID', '오늘의 맛집', 'C:/sample/uploads/image08.png', 28, 60, JSON_OBJECT('backgroundColor', '#F0F8FF', 'textColor', '#003366', 'fontFamily', 'Helvetica'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(9, 9, 'POLAROID', '야경이 예쁜 곳', 'C:/sample/uploads/image09.jpg', 30, 50, JSON_OBJECT('backgroundColor', '#FFF0F5', 'textColor', '#8B004B', 'fontFamily', 'Palatino Linotype'), '2026-05-28 14:32:18', '2026-05-31 14:32:18'),
(10, 10, 'POLAROID', '함께라서 특별한 하루', 'C:/sample/uploads/image10.png', 60, 40, JSON_OBJECT('backgroundColor', '#F8F8FF', 'textColor', '#2F2F4F', 'fontFamily', 'Arial Black'), '2026-05-28 14:32:18', '2026-06-02 14:32:18');

-- like 샘플
INSERT IGNORE INTO `like` (like_id, user_id, trace_id, created_at) VALUES
(1, 1, 1, '2026-05-27 14:32:18'),
(2, 4, 1, '2026-05-28 14:32:18'),
(3, 3, 3, '2026-05-29 14:32:18'),
(4, 5, 2, '2026-05-30 14:32:18'),
(5, 2, 5, '2026-05-31 14:32:18'),
(6, 6, 2, '2026-06-01 14:32:18'),
(7, 2, 7, '2026-06-02 14:32:18'),
(8, 8, 6, '2026-06-03 14:32:18'),
(9, 8, 3, '2026-06-04 14:32:18'),
(10, 7, 8, '2026-06-05 14:32:18');

-- user_follow sample
-- follower_id: user who follows, following_id: user being followed
INSERT IGNORE INTO user_follow (follow_id, follower_id, following_id, created_at) VALUES
(1, 1, 2, '2026-06-06 10:00:00'),
(2, 1, 3, '2026-06-06 10:05:00'),
(3, 2, 1, '2026-06-06 10:10:00'),
(4, 3, 2, '2026-06-06 10:15:00'),
(5, 4, 1, '2026-06-06 10:20:00'),
(6, 5, 2, '2026-06-06 10:25:00'),
(7, 6, 3, '2026-06-06 10:30:00'),
(8, 7, 4, '2026-06-06 10:35:00'),
(9, 8, 5, '2026-06-06 10:40:00'),
(10, 9, 6, '2026-06-06 10:45:00');

-- notification sample
-- receiver_id: user who receives notification, sender_id: user who created trace
INSERT IGNORE INTO notification (
    notification_id,
    receiver_id,
    sender_id,
    trace_id,
    notification_type,
    message,
    is_read,
    read_at,
    created_at
) VALUES
(1, 1, 2, 3, 'FOLLOWING_TRACE_CREATED', 'user2 created a new trace.', 0, NULL, '2026-06-06 11:00:00'),
(2, 1, 3, 6, 'FOLLOWING_TRACE_CREATED', 'user3 created a new trace.', 1, '2026-06-06 11:20:00', '2026-06-06 11:05:00'),
(3, 2, 1, 1, 'FOLLOWING_TRACE_CREATED', 'user1 created a new trace.', 0, NULL, '2026-06-06 11:10:00'),
(4, 3, 2, 4, 'FOLLOWING_TRACE_CREATED', 'user2 created a new trace.', 0, NULL, '2026-06-06 11:15:00'),
(5, 4, 1, 2, 'FOLLOWING_TRACE_CREATED', 'user1 created a new trace.', 1, '2026-06-06 11:40:00', '2026-06-06 11:20:00'),
(6, 5, 2, 5, 'FOLLOWING_TRACE_CREATED', 'user2 created a new trace.', 0, NULL, '2026-06-06 11:25:00'),
(7, 6, 3, 6, 'FOLLOWING_TRACE_CREATED', 'user3 created a new trace.', 0, NULL, '2026-06-06 11:30:00'),
(8, 7, 4, 7, 'FOLLOWING_TRACE_CREATED', 'user4 created a new trace.', 1, '2026-06-06 11:55:00', '2026-06-06 11:35:00'),
(9, 8, 5, 8, 'FOLLOWING_TRACE_CREATED', 'user5 created a new trace.', 0, NULL, '2026-06-06 11:40:00'),
(10, 9, 6, 9, 'FOLLOWING_TRACE_CREATED', 'user6 created a new trace.', 0, NULL, '2026-06-06 11:45:00');

-- report 샘플
INSERT IGNORE INTO report (report_id, user_id, trace_id, report_kind, created_at, updated_at) VALUES
(1, 9, 1, '악성 글', '2026-05-27 14:32:18', '2026-05-27 14:32:18'),
(2, 4, 7, '욕설', '2026-05-28 14:32:18', '2026-05-28 14:32:18'),
(3, 3, 7, '악성 글', '2026-05-29 14:32:18', '2026-05-29 14:32:18'),
(4, 5, 2, '악성 글', '2026-05-30 14:32:18', '2026-05-30 14:32:18'),
(5, 2, 10, '악성 글', '2026-05-31 14:32:18', '2026-05-31 14:32:18'),
(6, 6, 9, '악성 글', '2026-06-01 14:32:18', '2026-06-01 14:32:18'),
(7, 2, 1, '악성 글', '2026-06-02 14:32:18', '2026-06-02 14:32:18'),
(8, 8, 2, '욕설', '2026-06-03 14:32:18', '2026-06-03 14:32:18'),
(9, 8, 7, '욕설', '2026-06-04 14:32:18', '2026-06-04 14:32:18'),
(10, 7, 3, '욕설', '2026-06-05 14:32:18', '2026-06-05 14:32:18');
