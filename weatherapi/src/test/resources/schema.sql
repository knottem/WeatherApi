-- Create city table
CREATE TABLE `city` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `lat` double NOT NULL,
    `lon` double NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- Create auth table
CREATE TABLE `auth`
(
    `id`       BIGINT                              NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255)                        NOT NULL,
    `password` VARCHAR(255)                        NOT NULL,
    `role`     ENUM ('ADMIN', 'USER', 'SUPERUSER') NOT NULL,
    PRIMARY KEY (`id`)
);