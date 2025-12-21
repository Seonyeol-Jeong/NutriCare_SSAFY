-- 25.12.21 수정
-- 데이터베이스 생성 및 선택
DROP DATABASE IF EXISTS nutricare_db;
CREATE DATABASE nutricare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE nutricare_db;

------------------------------------------------------------
-- 1) USER: 사용자 계정
------------------------------------------------------------
CREATE TABLE `user` (
  `user_id`       BIGINT       NOT NULL AUTO_INCREMENT,
  `email`         VARCHAR(100) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `name`          VARCHAR(50)  NOT NULL,
  `birth_year`    YEAR         NULL,              -- 출생연도 (예: 1998)
  `gender`        ENUM('MALE','FEMALE','OTHER') NULL,
   `role`         ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0,
  `provider`		VARCHAR(20) DEFAULT NULL,
  `provider_id`		VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 2) HEALTH_PROFILE: 건강 정보 (user와 1:1)
------------------------------------------------------------
CREATE TABLE `health_profile` (
  `health_id`      BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT       NOT NULL,
  `height_cm`      DECIMAL(5,2) NULL,
  `weight_kg`      DECIMAL(5,2) NULL,
  `activity_level` ENUM('LOW','MEDIUM','HIGH') NULL,
  `goal_type`      ENUM('LOSE','MAINTAIN','GAIN') NULL,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`health_id`),
  UNIQUE KEY `uk_health_user` (`user_id`),
  CONSTRAINT `fk_health_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user`(`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 3) PHOTO: 얼굴 사진 (이미지 파일 정보)
------------------------------------------------------------
CREATE TABLE `photo` (
  `photo_id`    BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `photo_url`   VARCHAR(255) NOT NULL,            -- 기존 image_path 역할
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`photo_id`),
  KEY `idx_photo_user` (`user_id`),
  CONSTRAINT `fk_photo_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user`(`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 4) ANALYSIS_RESULT: 사진에 대한 분석 결과 (진단 라벨 등)
------------------------------------------------------------
CREATE TABLE `analysis_result` (
  `analysis_id`    BIGINT       NOT NULL AUTO_INCREMENT,
  `photo_id`       BIGINT       NOT NULL,
  `diagnosis_name` VARCHAR(50)  NOT NULL,         -- {'건선': 0, '아토피': 1, '여드름': 2, '정상': 3, '주사': 4, '지루': 5}
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`analysis_id`),
  KEY `idx_analysis_photo` (`photo_id`),
  CONSTRAINT `fk_analysis_photo`
    FOREIGN KEY (`photo_id`)
    REFERENCES `photo`(`photo_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 5) DIET_RECOMMENDATION: 식단 추천 헤더
------------------------------------------------------------
CREATE TABLE `diet_recommendation` (
  `rec_id`                  BIGINT       NOT NULL AUTO_INCREMENT,
  `health_id`                 BIGINT       NOT NULL,
  `analysis_id`             BIGINT       NULL,       -- analysis_result를 참조
  `created_at`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `memo`                    VARCHAR(255) NULL,
  PRIMARY KEY (`rec_id`),
  KEY `idx_rec_health` (`health_id`),
  KEY `idx_rec_analysis` (`analysis_id`),
  CONSTRAINT `fk_rec_user`
    FOREIGN KEY (`health_id`)
    REFERENCES `health_profile`(`health_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_rec_analysis_result`
    FOREIGN KEY (`analysis_id`)
    REFERENCES `analysis_result`(`analysis_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 6) DIET_RESULT (기존 DIET_MEAL)
-- meal_id → result_id 로 수정
-- skincare_url 삭제
------------------------------------------------------------
CREATE TABLE `diet_result` (
  `result_id`     BIGINT       NOT NULL AUTO_INCREMENT,  -- PK 변경됨
  `rec_id`        BIGINT       NOT NULL,                 -- FK: diet_recommendation.rec_id
  `menu_name`     VARCHAR(255) NOT NULL,                 -- 메뉴 이름
  `description`   TEXT         NULL,                     -- 설명
  `calories`      INT          NOT NULL DEFAULT 0,       -- 칼로리
  `notes`         VARCHAR(255) NULL,                     -- 기타 메모
  `recipe_url`    VARCHAR(255) NULL,                     -- 조리법 URL
  PRIMARY KEY (`result_id`),                             -- PK 이름 변경됨
  KEY `idx_result_rec` (`rec_id`),                       -- 인덱스도 의미 있게 변경
  CONSTRAINT `fk_result_rec`
    FOREIGN KEY (`rec_id`)
    REFERENCES `diet_recommendation`(`rec_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



------------------------------------------------------------
-- 7) BOARD: 게시판 글
------------------------------------------------------------
CREATE TABLE `board` (
  `board_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT       NOT NULL,
  `user_name`	VARCHAR(255) NOT NULL,
  `title`      VARCHAR(200) NOT NULL,
  `content`    TEXT         NOT NULL,
  `category`   VARCHAR(50)  NULL,
  `view_count` INT          NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`board_id`),
  KEY `idx_board_user` (`user_id`),
  CONSTRAINT `fk_board_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user`(`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


------------------------------------------------------------
-- 8) COMMENT: 게시판 댓글
------------------------------------------------------------
CREATE TABLE `comment` (
  `comment_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `board_id`   BIGINT       NOT NULL,
  `user_id`    BIGINT       NOT NULL,
  `user_name`	VARCHAR(255) NOT NULL,
  `content`    TEXT         NOT NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`comment_id`),
  KEY `idx_comment_board` (`board_id`),
  KEY `idx_comment_user` (`user_id`),
  CONSTRAINT `fk_comment_board`
    FOREIGN KEY (`board_id`)
    REFERENCES `board`(`board_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user`(`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 9) board_image: 게시판 댓글
------------------------------------------------------------

CREATE TABLE board_image (
  image_id   BIGINT       NOT NULL AUTO_INCREMENT,
  board_id   BIGINT       NOT NULL,
  image_url  VARCHAR(255) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (image_id),
  KEY idx_board_image_board (board_id),
  CONSTRAINT fk_board_image_board
    FOREIGN KEY (board_id)
    REFERENCES board(board_id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

