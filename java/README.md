# BeautyMinder 

Spring Boot v3.1 버전 (대충 로그인/회원가입/JWT 기능)

![demo](https://github.com/LeeZEun/beautyMinder/assets/2356749/53e4f4db-faee-467f-921e-7c2faf3c5f28)

```sql
-- Create the `users` table
CREATE TABLE users (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
INDEX (email)
);

-- Create the `refresh_token` table
CREATE TABLE refresh_token (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL UNIQUE,
refresh_token TEXT NOT NULL,
FOREIGN KEY (user_id) REFERENCES users(id),
INDEX (user_id)
);

-- Create the `diary` table
CREATE TABLE diary (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
title VARCHAR(255) NOT NULL,
content TEXT NOT NULL,
author VARCHAR(255) NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX (title),
INDEX (created_at),
INDEX (updated_at)
);


```