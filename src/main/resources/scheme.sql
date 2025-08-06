CREATE TABLE IF NOT EXISTS member
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    username           VARCHAR(255) NOT NULL UNIQUE,
    password           VARCHAR(255) NOT NULL,
    email              VARCHAR(255) NOT NULL UNIQUE,
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Role 테이블
CREATE TABLE IF NOT EXISTS role
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Member-Role 중간 테이블
CREATE TABLE IF NOT EXISTS member_role
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    role_id   BIGINT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    UNIQUE KEY unique_member_role (member_id, role_id)
);

-- AccessPage 테이블
CREATE TABLE IF NOT EXISTS access_page
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    path VARCHAR(255) NOT NULL UNIQUE
);

-- Role-Page-Permission 테이블
CREATE TABLE IF NOT EXISTS role_page_permission
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id        BIGINT NOT NULL,
    access_page_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    FOREIGN KEY (access_page_id) REFERENCES access_page (id) ON DELETE CASCADE,
    UNIQUE KEY unique_role_page (role_id, access_page_id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_member_username ON member (username);
CREATE INDEX IF NOT EXISTS idx_member_email ON member (email);
CREATE INDEX IF NOT EXISTS idx_role_name ON role (name);
CREATE INDEX IF NOT EXISTS idx_member_role_member_id ON member_role (member_id);
CREATE INDEX IF NOT EXISTS idx_member_role_role_id ON member_role (role_id);
CREATE INDEX IF NOT EXISTS idx_access_page_path ON access_page (path);
CREATE INDEX IF NOT EXISTS idx_role_page_permission_role_id ON role_page_permission (role_id);
CREATE INDEX IF NOT EXISTS idx_role_page_permission_access_page_id ON role_page_permission (access_page_id);