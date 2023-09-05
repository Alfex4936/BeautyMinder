# BeautyMinder 앱 데이터베이스 스키마

> TODO: MySQL or PostgreSQL

> 테이블 이름 복수형 (plural) 선택

## 테이블

### 1. User (사용자)

| 칼럼 이름       | 타입           | 설명                 | Nullable |
| -------------- | -------------- | -------------------- | -------- |
| id             | `BIGINT`       | 고유 아이디          | 아니오   |
| email          | `VARCHAR(255)` | 이메일               | 아니오   |
| password       | `VARCHAR(255)` | 암호화된 비밀번호    | 아니오   |
| nickname       | `VARCHAR(255)` | 닉네임               | 예       |
| profileImage   | `VARCHAR(255)` | 프로필 이미지 URL    | 예       |
| createdAt      | `LocalDateTime`| 생성 일시            | 아니오   |

🔗 **연관 테이블**: `Cosmetic`, `RefreshToken`, `Todo`

### 2. Cosmetic (화장품)

| 칼럼 이름       | 타입           | 설명                 | Nullable |
| -------------- | -------------- | -------------------- | -------- |
| id             | `BIGINT`       | 고유 아이디          | 아니오   |
| name           | `VARCHAR(100)` | 화장품 이름          | 아니오   |
| expirationDate | `LocalDate`    | 유통기한             | 예       |
| createdDate    | `LocalDateTime`| 생성 일시            | 아니오   |
| purchasedDate  | `LocalDate`    | 구입 일자            | 예       |
| category       | `ENUM`         | 카테고리             | 아니오   |
| status         | `ENUM`         | 개봉/미개봉          | 아니오   |
| user_id        | `BIGINT`       | User 테이블의 FK     | 아니오   |

```sql
category        ENUM('스킨케어', '클렌징/필링', '마스크/팩', '선케어', '베이스', '아이', '립', '바디', '헤어', '네일', '향수', '기타')
status          ENUM('개봉', '미개봉')
```

🔗 **연관 테이블**: `User`

### 3. RefreshToken (리프레시 토큰)

| 칼럼 이름       | 타입           | 설명                 | Nullable |
| -------------- | -------------- | -------------------- | -------- |
| id             | `BIGINT`       | 고유 아이디          | 아니오   |
| user_id        | `BIGINT`       | User 테이블의 FK     | 아니오   |
| refreshToken   | `VARCHAR(255)` | 리프레시 토큰        | 아니오   |
| createdAt      | `LocalDateTime`| 생성 일시            | 아니오   |
| expiresAt      | `LocalDateTime`| 만료 일시            | 예       |

🔗 **연관 테이블**: `User`

### 4. Todo (할 일)

| 칼럼 이름       | 타입           | 설명                 | Nullable |
| -------------- | -------------- | -------------------- | -------- |
| id             | `BIGINT`       | 고유 아이디          | 아니오   |
| date           | `LocalDate`    | 할 일의 날짜         | 아니오   |
| tasks          | `VARCHAR(255)` | 할 일 목록           | 아니오   |
| user_id        | `BIGINT`       | User 테이블의 FK     | 아니오   |

🔗 **연관 테이블**: `User`

## 관계도

```json
User 1 ----< Cosmetic
     1 ----< RefreshToken
     1 ----< Todo
```

> `User` 테이블과 다른 테이블 간의 1:다 관계를 나타냄.

> 각 사용자는 여러 개의 `Cosmetic`, `RefreshToken`, `Todo` 엔트리를 가질 수 있음.
