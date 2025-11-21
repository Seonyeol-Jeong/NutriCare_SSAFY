-- 25.11.21. 3:30 수정
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
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0,
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
  `diagnosis_name` VARCHAR(50)  NOT NULL,         -- 아토피 등 라벨
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
  `user_id`                 BIGINT       NOT NULL,
  `analysis_id`             BIGINT       NULL,       -- analysis_result를 참조
  `target_calories`         INT          NULL,
  `days`                    INT          NULL,
  `start_date`              DATE         NULL,
  `created_at`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `memo`                    VARCHAR(255) NULL,
  PRIMARY KEY (`rec_id`),
  KEY `idx_rec_user` (`user_id`),
  KEY `idx_rec_analysis` (`analysis_id`),
  CONSTRAINT `fk_rec_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user`(`user_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_rec_analysis_result`
    FOREIGN KEY (`analysis_id`)
    REFERENCES `analysis_result`(`analysis_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

------------------------------------------------------------
-- 6) DIET_MEAL: 식단 추천 상세(일자/끼니별)
------------------------------------------------------------
CREATE TABLE `diet_meal` (
  `meal_id`    BIGINT       NOT NULL AUTO_INCREMENT,
  `rec_id`     BIGINT       NOT NULL,
  `day_index`  INT          NOT NULL,   -- N일차
  `meal_type`  ENUM('BREAKFAST','LUNCH','DINNER','SNACK') NOT NULL,
  `menu_name`  VARCHAR(255) NOT NULL,
  `description` TEXT        NULL,
  `calories`   INT          NULL,
  `notes`      VARCHAR(255) NULL,
  PRIMARY KEY (`meal_id`),
  KEY `idx_meal_rec` (`rec_id`),
  CONSTRAINT `fk_meal_rec`
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
-- 9) BOARD_IMAGE: 게시글 이미지
------------------------------------------------------------
CREATE TABLE `board_image` (
  `image_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `board_id`   BIGINT       NOT NULL,
  `image_url`  VARCHAR(255) NOT NULL,   -- 이미지 경로(URL 또는 S3 Key)
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_board_image_board` (`board_id`),
  CONSTRAINT `fk_board_image_board`
    FOREIGN KEY (`board_id`)
    REFERENCES `board`(`board_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
