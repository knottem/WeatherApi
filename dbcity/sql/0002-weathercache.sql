-- noinspection SqlNoDataSourceInspectionForFile

-- NOT FINISHED, started this if I want the cache to be saved on the db instead of just in the program.

USE `weatherdb`;

DROP TABLE IF EXISTS `cache`;

CREATE TABLE `cache` (
    `id` bigint NOT NULL,
    `lat` double NOT NULL,
    `lon` double NOT NULL,
    `message` varchar(255),
    `temperature` double NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;