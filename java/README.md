# BeautyMinder 

Spring Boot v3.1 버전 (대충 로그인/회원가입/JWT 기능)

![demo](https://github.com/LeeZEun/beautyMinder/assets/2356749/88d8eb9b-1091-473d-96b5-a293f78ea337)

```sql
-- Create the `users` table
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX (email)
);

-- Create the `refresh_token` table
CREATE TABLE refresh_token
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE,
    refresh_token TEXT   NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at    DATETIME,
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX (user_id)
);


-- Create the `cosmetics` table
CREATE TABLE cosmetics
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100),
    expiration_date DATE,
    created_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    purchased_date  DATE,
    category        ENUM('스킨케어', '클렌징/필링', '마스크/팩', '선케어', '베이스', '아이', '립', '바디', '헤어', '네일', '향수', '기타'),
    user_id         BIGINT NOT NULL,
    status          ENUM('개봉', '미개봉'),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Create the `todos` table
CREATE TABLE todos
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    date    DATE,
    tasks   TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
```

## Old
```sql
-- Create the `diaries` table
CREATE TABLE diaries
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    author     VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (title),
    INDEX (created_at),
    INDEX (updated_at)
);
```