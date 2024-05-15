INSERT INTO `city` (`id`,`lat`, `lon`, `name`)VALUES
    ('2785714e-0872-4a61-bfb5-76b5baf8911b',59.3294,18.0686,'Stockholm'), -- 1
    ('82f45775-ed0a-44e4-afe6-a07d97e9663c',57.7075,11.9675,'Göteborg'), -- 2
    ('c64ebe63-d5e2-448d-bb34-33cfe0a82fb9',55.6058,13.0358,'Malmö'), -- 3
    ('9399718a-b5c4-47c6-9073-6a6a55eac2c6',59.8581,17.6447,'Uppsala'), -- 4 - Used for delete test Case sensitive
    ('f48d1ac1-7831-405f-844c-f4ed9764682a',58.6,16.2,'Norrköping'), -- 5 - Used for delete test
    ('5e2eb58e-bf8b-4a5b-a92d-9b75faa387cc',59.6161,16.5528,'Västerås'), -- 6
    ('c9dad9cf-3e29-4362-9dd8-a6ff6f257e84',59.2739,15.2075,'Örebro'), -- 7
    ('e2c19dc8-86e3-4512-8055-6bda67ae3c1b',58.4158,15.6253,'Linköping'), -- 8 - Used for delete test UTF-8
    ('a79ca78e-0a61-407b-b4a9-7037a66c8052',56.05,12.7167,'Helsingborg'), -- 9
    ('58a46fab-8c11-411b-abf3-c941d68b9edc',57.7828,14.1606,'Jönköping'), -- 10
    ('08f0978e-c7de-455d-994f-a74c24289b92',57.6667,15.85,'Vimmerby'), -- 11
    ('89da212f-a051-4905-acfe-1c52d537f45f',62.4,17.3167,'Sundsvall'), -- 12
    ('55cc986f-4d7c-44e2-9e6f-d2160942e0e7',60.6747,17.1417,'Gävle'), -- 13
    ('b00290a6-a66e-466a-b27d-06c0d56b0691',63.825,20.2639,'Umeå'), -- 14
    ('a8068ada-ccd8-4260-905c-b0bf5bd60ed9',64.75,20.95,'Skellefteå'), -- 15
    ('26654284-92ca-4c60-b95f-3e1160701cb0',59.2572,18.0319,'Rågsved'); -- 16

INSERT INTO `auth` (`id`, `username`, `password`, `role`)VALUES
    ('232d5741-f551-443a-a1b0-b7b1eb26ba9f','admin','$2a$10$Eq3yxcRlqmIaxBv7w9qkDuZCfw7lhgYS12ndADouoUogkNMWGmELK', 'ADMIN' ), -- pass123 for testing
    ('2f42da30-7b17-4718-8782-563b4cc6c4c7','user','$2a$10$OMsvTvB3K5kHX6cTrSDEIuMFfKj2XASnIUPe/jPw.Fk6Vk9zQNK5W', 'USER'); -- pass123 for testing