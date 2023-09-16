-- Added a few users for testing purposes, can be extended with more users.

USE `weatherdb`;
DROP TABLE IF EXISTS `auth`;
CREATE TABLE `auth`
(
    `id`       BIGINT                              NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255)                        NOT NULL,
    `password` VARCHAR(255)                        NOT NULL,
    `role`     ENUM ('ADMIN', 'USER', 'SUPERUSER') NOT NULL,
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `auth` (`username`, `password`, `role`) VALUES
    ('admin','$2a$10$Eq3yxcRlqmIaxBv7w9qkDuZCfw7lhgYS12ndADouoUogkNMWGmELK', 'ADMIN' ), -- pass123 for testing
    ('user','$2a$10$OMsvTvB3K5kHX6cTrSDEIuMFfKj2XASnIUPe/jPw.Fk6Vk9zQNK5W', 'USER'); -- pass123 for testing