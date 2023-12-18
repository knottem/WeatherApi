-- Create city table
CREATE TABLE `city` (
`id` VARCHAR(36) NOT NULL,
`lat` double NOT NULL,
`lon` double NOT NULL,
`name` varchar(255) DEFAULT NULL,
PRIMARY KEY (`id`)
);

-- Create auth table
CREATE TABLE `auth`
(
    `id` VARCHAR(36) NOT NULL,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role`     ENUM ('ADMIN', 'USER', 'SUPERUSER') NOT NULL,
    PRIMARY KEY (`id`)
);