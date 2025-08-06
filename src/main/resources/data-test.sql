-- 기본 역할 데이터
INSERT INTO role (id, name)
VALUES (1, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name = name;
INSERT INTO role (id, name)
VALUES (2, 'ROLE_USER')
ON DUPLICATE KEY UPDATE name = name;
INSERT INTO role (id, name)
VALUES (3, 'ROLE_GUEST')
ON DUPLICATE KEY UPDATE name = name;

-- 기본 페이지 경로 데이터
INSERT INTO access_page (id, path)
VALUES (1, '/admin/**')
ON DUPLICATE KEY UPDATE path = path;
INSERT INTO access_page (id, path)
VALUES (2, '/play/**')
ON DUPLICATE KEY UPDATE path = path;
INSERT INTO access_page (id, path)
VALUES (3, '/chat/**')
ON DUPLICATE KEY UPDATE path = path;
INSERT INTO access_page (id, path)
VALUES (4, '/user/**')
ON DUPLICATE KEY UPDATE path = path;
INSERT INTO access_page (id, path)
VALUES (5, '/member/**')
ON DUPLICATE KEY UPDATE path = path;

-- 기본 테스트 사용자 (패스워드는 BCrypt로 인코딩된 값)
-- admin 계정 (비밀번호: admin123)
INSERT INTO member (id, username, password, email)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKQng23flPOtJQfiNKjCr6Mm2',
        'admin@example.com')
ON DUPLICATE KEY UPDATE username = username;

-- user 계정 (비밀번호: user123)
INSERT INTO member (id, username, password, email)
VALUES (2, 'user', '$2a$10$9.LbN4QZCnMD6cxIZ3qV8e1KGhQZqCr85KEzP0NQQX7BDzJOtTt0O',
        'user@example.com')
ON DUPLICATE KEY UPDATE username = username;

-- 사용자-역할 매핑
INSERT INTO member_role (member_id, role_id)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE member_id = member_id; -- admin - ROLE_ADMIN
INSERT INTO member_role (member_id, role_id)
VALUES (2, 2)
ON DUPLICATE KEY UPDATE member_id = member_id;
-- user - ROLE_USER

-- 역할-페이지 권한 매핑
-- ADMIN 권한 (모든 페이지 접근 가능)
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id = role_id; -- ADMIN - /admin/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (1, 2)
ON DUPLICATE KEY UPDATE role_id = role_id; -- ADMIN - /play/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (1, 3)
ON DUPLICATE KEY UPDATE role_id = role_id; -- ADMIN - /chat/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (1, 4)
ON DUPLICATE KEY UPDATE role_id = role_id; -- ADMIN - /user/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (1, 5)
ON DUPLICATE KEY UPDATE role_id = role_id;
-- ADMIN - /member/**

-- USER 권한 (제한된 페이지만 접근 가능)
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (2, 2)
ON DUPLICATE KEY UPDATE role_id = role_id; -- USER - /play/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (2, 4)
ON DUPLICATE KEY UPDATE role_id = role_id; -- USER - /user/**
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (2, 5)
ON DUPLICATE KEY UPDATE role_id = role_id;
-- USER - /member/**

-- GUEST 권한 (매우 제한된 접근)
INSERT INTO role_page_permission (role_id, access_page_id)
VALUES (3, 2)
ON DUPLICATE KEY UPDATE role_id = role_id; -- GUEST - /play/**