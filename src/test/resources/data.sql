INSERT INTO `city` (`lat`, `lon`, `name`)VALUES
    (59.3294,18.0686,'Stockholm'), -- 1
    (57.7075,11.9675,'Göteborg'), -- 2
    (55.6058,13.0358,'Malmö'), -- 3
    (59.8581,17.6447,'Uppsala'), -- 4
    (58.6,16.2,'Norrköping'), -- 5
    (59.8601,17.64,'Uppsala'), -- 6
    (59.6161,16.5528,'Västerås'), -- 7
    (59.2739,15.2075,'Örebro'); -- 8

INSERT INTO `auth` (`username`, `password`, `role`)VALUES
    ('admin','$2a$10$Eq3yxcRlqmIaxBv7w9qkDuZCfw7lhgYS12ndADouoUogkNMWGmELK', 'ADMIN' ), -- pass123 for testing
    ('user','$2a$10$OMsvTvB3K5kHX6cTrSDEIuMFfKj2XASnIUPe/jPw.Fk6Vk9zQNK5W', 'USER'); -- pass123 for testing