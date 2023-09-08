-- noinspection SqlNoDataSourceInspectionForFile
-- Simple database with lon and lat for locations in sweden, can be easily extended with just adding new values.
-- Added a few users for testing purposes, can be extended with more users.

CREATE SCHEMA IF NOT EXISTS `weatherdb`;
USE `weatherdb`;

DROP TABLE IF EXISTS `auth`;

CREATE TABLE `auth` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `auth` VALUES
    (1,'admin','$2a$10$Eq3yxcRlqmIaxBv7w9qkDuZCfw7lhgYS12ndADouoUogkNMWGmELK', 'admin'), -- pass123 for testing
    (2,'user','$2a$10$OMsvTvB3K5kHX6cTrSDEIuMFfKj2XASnIUPe/jPw.Fk6Vk9zQNK5W', 'user');

DROP TABLE IF EXISTS `city`;
CREATE TABLE `city` (
  `id` bigint NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `city` VALUES 
    (1,59.3294,18.0686,'Stockholm'),
    (2,57.7075,11.9675,'Göteborg'),
    (3,55.6058,13.0358,'Malmö'),
    (4,59.8581,17.6447,'Uppsala'),
    (5,58.6,16.2,'Norrköping'),
    (6,59.8601,17.64,'Uppsala'),
    (7,59.6161,16.5528,'Västerås'),
    (8,59.2739,15.2075,'Örebro'),
    (9,58.4158,15.6253,'Linköping'),
    (10,56.05,12.7167,'Helsingborg'),
    (11,57.7828,14.1606,'Jönköping'),
    (12,57.6667,15.85,'Vimmerby'),
    (13,62.4,17.3167,'Sundsvall'),
    (14,60.6747,17.1417,'Gävle'),
    (15,63.825,20.2639,'Umeå'),
    (16,64.75,20.95,'Skellefteå'),
    (17,59.3783,13.5042,'Karlstad'),
    (18,59.1958,17.6281,'Södertälje'),
    (19,56.6739,12.8572,'Halmstad'),
    (20,59.3708,16.5097,'Eskilstuna'),
    (21,56.1608,15.5861,'Karlskrona'),
    (22,56.8769,14.8092,'Växjö'),
    (23,57.7211,12.9403,'Borås'),
    (24,59.4333,18.0833,'Täby'),
    (25,58.2828,12.2892,'Trollhättan'),
    (26,63.1792,14.6358,'Östersund'),
    (27,65.5844,22.1539,'Luleå'),
    (28,59.5167,17.9167,'Upplands Väsby'),
    (29,60.4856,15.4364,'Borlänge'),
    (30,55.3667,13.1667,'Trelleborg'),
    (31,56.6614,16.3628,'Kalmar'),
    (32,58.5,13.1833,'Lidköping'),
    (33,58.3833,13.85,'Skövde'),
    (34,58.7531,17.0086,'Nyköping'),
    (35,57.93,12.5331,'Alingsås'),
    (36,59.2,17.8167,'Tumba'),
    (37,60.6072,15.6311,'Falun'),
    (38,57.65,12.0167,'Mölndal'),
    (39,57.75,16.6333,'Västervik'),
    (40,57.1167,12.2167,'Varberg'),
    (41,58.35,11.9167,'Uddevalla'),
    (42,55.8706,12.8311,'Landskrona'),
    (43,63.2908,18.7156,'Örnsköldsvik'),
    (44,55.7939,13.1133,'Kävlinge'),
    (45,56.0337,14.1333,'Kristianstad'),
    (46,59.3667,18.15,'Lidingö'),
    (47,58.5333,15.0333,'Motala'),
    (48,59.5333,18.0833,'Vallentuna'),
    (49,55.4167,13.8333,'Ystad'),
    (50,59.4833,18.3,'Åkersberga'),
    (51,56.9053,12.4911,'Falkenberg'),
    (52,59.3333,14.5167,'Karlskoga'),
    (53,59.6167,17.85,'Märsta'),
    (54,67.8489,20.3028,'Kiruna'),
    (55,57.629,18.3071,'Visby'),
    (56,59,16.2,'Katrineholm'),
    (57,59.3167,18.25,'Boo'),
    (58,58.3806,12.325,'Vänersborg'),
    (59,65.3333,21.5,'Piteå'),
    (60,57.4833,12.0667,'Kungsbacka'),
    (61,60.6167,16.7833,'Sandviken'),
    (62,57.8667,11.9667,'Kungälv'),
    (63,57.7919,14.2756,'Huskvarna'),
    (64,59.1167,15.1333,'Kumla'),
    (65,59.6356,17.0764,'Enköping'),
    (66,55.6333,13.7167,'Sjöbo'),
    (67,56.1667,14.85,'Karlshamn'),
    (68,58.3833,13.4333,'Skara'),
    (69,62.6361,17.9411,'Härnösand'),
    (70,56.1667,13.7667,'Hässleholm'),
    (71,59.3,14.1167,'Kristinehamn'),
    (72,55.8392,13.3039,'Eslöv'),
    (73,59.5167,15.9833,'Köping'),
    (74,59.7667,18.7,'Norrtälje'),
    (75,57.265,16.45,'Oskarshamn'),
    (76,57.7667,12.3,'Lerum'),
    (77,65.8256,21.6906,'Boden'),
    (78,56.1167,13.15,'Klippansbruk'),
    (79,57.65,14.6833,'Nässjö'),
    (80,58.7,13.8167,'Mariestad'),
    (81,58.175,13.5531,'Falköping'),
    (82,56.2,12.5667,'Höganäs'),
    (83,59.2,17.9,'Tullinge'),
    (84,57.6667,12.1167,'Mölnlycke'),
    (85,57.5167,12.6833,'Kinna'),
    (86,59.3667,17.0333,'Strängnäs'),
    (87,55.55,12.9167,'Bunkeflostrand'),
    (88,56.8333,13.9333,'Ljungby'),
    (89,59.6167,16.25,'Hallstahammar'),
    (90,59.1167,18.0667,'Västerhaninge'),
    (91,61.7333,17.1167,'Hudiksvall'),
    (92,55.6333,13.2,'Staffanstorp'),
    (93,60.1333,15.1833,'Ludvika'),
    (94,59.6542,12.5914,'Arvika'),
    (95,58.0333,14.9667,'Tranås'),
    (96,58.3333,15.1167,'Mjölby'),
    (97,55.6667,13.0833,'Lomma'),
    (98,56.7333,15.9,'Nybro'),
    (99,58.9,17.95,'Nynäshamn'),
    (100,59.3939,15.8386,'Arboga'),
    (101,61.3481,16.3947,'Bollnäs'),
    (102,59.3333,13.4333,'Skoghall'),
    (103,59.5833,17.5,'Bålsta'),
    (104,57.4333,15.0667,'Vetlanda'),
    (105,59.8333,13.1333,'Sunne'),
    (106,61.0167,14.5333,'Mora'),
    (107,58.7,15.8,'Finspång'),
    (108,59.9167,16.6,'Sala'),
    (109,60.0333,13.65,'Hagfors'),
    (110,56.2,15.2833,'Ronneby'),
    (111,60.1456,16.1683,'Avesta'),
    (112,61.3,17.0833,'Söderhamn'),
    (113,59.2833,17.8,'Ekerö'),
    (114,58.6667,17.1167,'Oxelösund'),
    (115,57.7833,13.4167,'Ulricehamn'),
    (116,59.3333,18.3833,'Gustavsberg'),
    (117,60.35,15.75,'Säter'),
    (118,60.0042,15.7933,'Fagersta'),
    (119,57.5667,12.1,'Lindome'),
    (120,59.7167,14.1667,'Filipstad'),
    (121,55.5,13.2333,'Svedala'),
    (122,55.4167,12.95,'Höllviken'),
    (123,62.4869,17.3258,'Timrå'),
    (124,60.6,15.0833,'Gagnef'),
    (125,56.2833,13.2833,'Örkelljunga'),
    (126,59.15,18.1333,'Jordbro'),
    (127,57.5833,11.9333,'Billdal'),
    (128,55.6333,13.0833,'Arlöv'),
    (129,57.3,13.5333,'Gislaved'),
    (130,59.25,18.1833,'Älta'),
    (131,58.0833,11.8167,'Stenungsund'),
    (132,57.9,12.0667,'Nödinge-Nol'),
    (133,57.6669,14.9703,'Eksjö'),
    (134,59.8167,17.7,'Sävja'),
    (135,59.5833,15.25,'Lindesberg'),
    (136,55.7167,13.0167,'Bjärred'),
    (137,59.2861,18.2872,'Saltsjöbaden'),
    (138,56.1347,12.9472,'Åstorp'),
    (139,55.9167,14.2833,'Åhus'),
    (140,59.4833,17.75,'Kungsängen'),
    (141,59.05,12.7,'Åmål'),
    (142,59.1333,12.9333,'Säffle'),
    (143,56.55,14.1333,'Älmhult'),
    (144,59.35,18.2,'Brevik'),
    (145,57.9167,14.0667,'Habo'),
    (146,63.1667,17.2667,'Sollefteå'),
    (147,58.4167,14.1667,'Tibro'),
    (148,64.6,18.6667,'Lycksele'),
    (149,67.13,20.66,'Gällivare'),
    (150,59.6167,17.7167,'Sigtuna'),
    (151,56.0442,14.5753,'Sölvesborg'),
    (152,58.1833,13.95,'Tidaholm'),
    (153,56.1344,13.1283,'Klippan'),
    (154,57.85,14.1167,'Bankeryd'),
    (155,57.8167,12.3667,'Floda'),
    (156,56.9,14.55,'Alvesta'),
    (157,58.2833,11.4333,'Lysekil'),
    (158,55.9361,13.5472,'Höör'),
    (159,59.5,13.3167,'Kil'),
    (160,59.4167,16.4667,'Torshälla'),
    (161,56.05,14.4667,'Bromölla'),
    (162,55.4833,13.5,'Skurup'),
    (163,59.175,17.4333,'Nykvarn'),
    (164,59.2917,18.2528,'Fisksätra'),
    (165,58.9333,11.1667,'Strömstad'),
    (166,57.6,12.05,'Kållered'),
    (167,56.2667,14.5333,'Olofström'),
    (168,65.85,23.1667,'Kalix'),
    (169,60.2769,15.9872,'Hedemora'),
    (170,59.1805,18.1804,'Vendelsö'),
    (171,59.2361,14.4297,'Degerfors'),
    (172,56.3667,13.9833,'Osby'),
    (173,57.6833,12.2,'Landvetter'),
    (174,56.1333,13.3833,'Perstorp'),
    (175,59.0667,15.1167,'Hallsberg'),
    (176,55.85,13.65,'Hörby'),
    (177,59.7167,17.8,'Knivsta'),
    (178,60.65,17.0333,'Valbo'),
    (179,59.5167,17.65,'Bro'),
    (180,58.4833,16.3167,'Söderköping'),
    (181,60.0833,15.95,'Norberg'),
    (182,55.4,12.85,'Skanör med Falsterbo'),
    (183,58.2,16,'Åtvidaberg'),
    (184,56.0833,12.9167,'Bjuv'),
    (185,55.4667,13.0167,'Vellinge'),
    (186,60.55,16.2833,'Hofors'),
    (187,58.5083,15.5028,'Ljungsbro'),
    (188,59.1903,18.1275,'Vega'),
    (189,65.8333,24.1333,'Haparanda'),
    (190,55.55,14.35,'Simrishamn'),
    (191,56.5167,13.0333,'Laholm'),
    (192,59.5167,15.0333,'Nora'),
    (193,63.15,14.75,'Torvalla'),
    (194,55.9667,12.7667,'Rydebäck'),
    (195,55.55,13.95,'Tomelilla'),
    (196,58.3,14.2833,'Hjo'),
    (197,59.9667,17.7,'Storvreta'),
    (198,57.1667,15.3333,'Åseda'),
    (199,55.7667,13.0167,'Löddeköpinge'),
    (200,61.8333,16.0833,'Ljusdal'),
    (201,59.5333,13.4667,'Forshaga'),
    (202,59.05,16.5833,'Flen'),
    (203,59.4167,16.0833,'Kungsör'),
    (204,59.7167,16.2167,'Surahammar'),
    (205,60.3333,17.5,'Tierp'),
    (206,55.7167,13.35,'Södra Sandby'),
    (207,60.6333,17.4167,'Skutskär'),
    (208,62.9333,17.8,'Kramfors'),
    (209,60.7333,15,'Leksands-Noret'),
    (210,58.0333,12.8,'Vårgårda'),
    (211,57.8167,12.0167,'Surte'),
    (212,55.6667,13.1667,'Hjärup'),
    (213,57.4833,15.8333,'Hultsfred'),
    (214,55.65,13.1167,'Åkarp'),
    (215,58.45,14.9,'Vadstena'),
    (216,59.05,17.3,'Gnesta'),
    (217,56.7333,16.3,'Lindsdal'),
    (218,63.7083,20.3694,'Holmsund'),
    (219,57.9167,13.8833,'Mullsjö'),
    (220,59.4333,13.4333,'Skåre'),
    (221,61.1167,14.6167,'Orsa'),
    (222,59.3167,18.5,'Hemmesta'),
    (223,58.4167,15.5167,'Malmslätt'),
    (224,56.4333,12.8333,'Båstad'),
    (225,60.6833,13.7333,'Malung'),
    (226,57.4,14.6667,'Sävsjö'),
    (227,57.7667,11.7,'Hönö'),
    (228,60.1333,15.4167,'Smedjebacken'),
    (229,58.9,17.55,'Trosa'),
    (230,59.35,13.1,'Grums'),
    (231,56.65,16.4833,'Färjestaden'),
    (232,62.4333,17.4167,'Vi'),
    (233,65.6833,21.0167,'Älvsbyn'),
    (234,57.2833,13.6333,'Anderstorp'),
    (235,65.6417,22.0139,'Gammelstaden'),
    (236,58.5333,13.4833,'Götene'),
    (237,57.8167,13.0167,'Fristad'),
    (238,56.1,12.7333,'Ödåkra'),
    (239,57.5,14.1167,'Vaggeryd'),
    (240,58.35,12.4,'Vargön'),
    (241,58.6167,16.25,'Lindö'),
    (242,58.1333,12.1333,'Lilla Edet'),
    (243,59.4167,18.3167,'Vaxholm'),
    (244,56.6167,15.55,'Emmaboda'),
    (245,58.7167,14.1333,'Töreboda'),
    (246,57.0333,16.45,'Mönsterås'),
    (247,60.8833,15.1333,'Rättvik'),
    (248,56.1592,13.6008,'Tyringe'),
    (249,65.5833,19.1667,'Arvidsjaur'),
    (250,59.7444,18.3687,'Rimbo'),
    (251,56.2333,15.2833,'Kallinge'),
    (252,60.2667,18.3667,'Östhammar'),
    (253,59.7667,14.5167,'Hällefors'),
    (254,57.1667,13.4,'Smålandsstenar'),
    (255,60.05,18.6,'Hallstavik'),
    (256,63.9167,19.75,'Vännäs'),
    (257,57.6833,14.0833,'Taberg'),
    (258,59.4333,18,'Sjöberg'),
    (259,58.6667,16.4,'Krokek'),
    (260,59.0333,15.8667,'Vingåker'),
    (261,58.2667,12.95,'Vara'),
    (262,56.15,12.5667,'Viken'),
    (263,59.8667,14.9833,'Kopparberg'),
    (264,57.95,12.1167,'Älvängen'),
    (265,57.8333,12.2833,'Gråbo'),
    (266,55.7667,13.1,'Furulund'),
    (267,56.8016,12.9763,'Oskarström'),
    (268,58.0833,13.0333,'Herrljunga'),
    (269,56.1,12.6333,'Hittarp'),
    (270,55.6333,13.4833,'Veberöd'),
    (271,58.5667,15.9,'Skärblacka'),
    (272,60.1333,13,'Torsby'),
    (273,61.3833,15.8167,'Edsbyn'),
    (274,56.1,13.9167,'Vinslöv'),
    (275,64.7167,21.1667,'Ursviken'),
    (276,56.45,13.5833,'Markaryd'),
    (277,57.7,11.7167,'Hjuvik'),
    (278,59.5,17.75,'Brunna'),
    (279,58.9833,17.9,'Ösmo'),
    (280,63.0833,14.8167,'Brunflo'),
    (281,57.4333,14.0833,'Skillingaryd'),
    (282,58.8797,14.9028,'Askersund'),
    (283,58.7333,17.0167,'Arnö'),
    (284,59.35,18.2833,'Kummelnäs'),
    (285,57.9833,15.6167,'Kisa'),
    (286,57.6333,12.8333,'Viskafors'),
    (287,57.5,13.1167,'Svenljunga'),
    (288,58.7,12.4667,'Mellerud'),
    (289,58.5333,14.5167,'Karlsborg'),
    (290,59.2667,17.2167,'Mariefred'),
    (291,58.4833,11.6833,'Munkedal'),
    (292,57,13.2417,'Hyltebruk'),
    (293,56.1833,14.7333,'Mörrum'),
    (294,58.35,15.2833,'Mantorp'),
    (295,64.6167,16.65,'Vilhelmina'),
    (296,65.575,22.1042,'Bergnäset'),
    (297,58.35,13.8333,'Skultorp'),
    (298,55.9167,13.1167,'Svalöv'),
    (299,63.85,15.5833,'Strömsund'),
    (300,57.6667,12.5667,'Bollebygd'),
    (301,56.25,14.0667,'Broby'),
    (302,63.3938,12.9351,'Åre'),
    (303,57.7,12.1167,'Öjersjö'),
    (304,55.7833,12.9667,'Hofterup'),
    (305,57.7,11.65,'Öckerö'),
    (306,56.6792,16.2356,'Smedby'),
    (307,60.0833,14.9833,'Grängesberg'),
    (308,55.5167,13,'Tygelsjö'),
    (309,55.5833,13.2,'Bara'),
    (310,56.25,15.6,'Rödeby'),
    (311,57.8,12.15,'Olofstorp'),
    (312,58.2167,11.9167,'Ljungskile'),
    (313,57.35,12.1167,'Åsa'),
    (314,57.8333,14.8,'Aneby'),
    (315,57.7167,13.0833,'Dalsjöfors'),
    (316,61.6333,17.0667,'Iggesund'),
    (317,59.3333,17.6667,'Stenhamra'),
    (318,57.4833,13.35,'Tranemo'),
    (319,58.95,17.5167,'Vagnhärad'),
    (320,59.3922,16.4203,'Hällbybrunn'),
    (321,55.9333,13.9667,'Tollarp'),
    (322,60.0333,17.55,'Björklinge'),
    (323,56.0167,12.95,'Ekeby'),
    (324,59.3833,12.1333,'Årjäng'),
    (325,62.35,17.0333,'Matfors'),
    (326,63.4,13.0833,'Åre'),
    (327,58.2,15.05,'Boxholm'),
    (328,58,11.55,'Skärhamn'),
    (329,64.6833,21.2333,'Skelleftehamn'),
    (330,57.5,11.9333,'Särö'),
    (331,57.7167,12.8,'Sandared'),
    (332,57.7167,14.3167,'Tenhult'),
    (333,65.7667,21.7333,'Råneå'),
    (334,58.4,15.0833,'Skänninge'),
    (335,59.7167,16.4167,'Skultuna'),
    (336,56.2,15.5167,'Nättraby'),
    (337,59.0292,12.2264,'Bengtsfors'),
    (338,56.8792,16.6558,'Borgholm'),
    (339,58.9833,14.6167,'Laxå'),
    (340,59.8333,13.5333,'Munkfors'),
    (341,56.5306,14.9769,'Tingsryd'),
    (342,56.1833,14.0833,'Knislinge'),
    (343,56.7833,15.1333,'Hovmantorp'),
    (344,65.6667,21.95,'Södra Sunderbyn'),
    (345,58.3319,12.6811,'Grästorp'),
    (346,58.65,16,'Svärtinge'),
    (347,59.6667,16.5833,'Hökåsen'),
    (348,59.4333,18.3333,'Resarö'),
    (349,58.9167,11.9167,'Ed'),
    (350,56.65,12.9167,'Fyllinge'),
    (351,56.05,13,'Billesholm'),
    (352,55.6,13.4,'Genarp'),
    (353,59.25,17.0833,'Åkers Styckebruk'),
    (354,58.5667,15.2833,'Borensberg'),
    (355,62.5167,15.6167,'Ånge'),
    (356,56.25,12.9667,'Munka-Ljungby'),
    (357,57.7167,12.8333,'Sjömarken'),
    (358,58.3667,11.25,'Kungshamn'),
    (359,58.4333,15.7833,'Linghem'),
    (360,55.85,12.9167,'Häljarp'),
    (361,56.0333,12.8167,'Påarp'),
    (362,66.6167,19.8333,'Jokkmokk'),
    (363,59.35,15.2167,'Hovsta'),
    (364,57.8833,16.3833,'Gamleby'),
    (365,58.2,16.6,'Valdemarsvik'),
    (366,59.4,13.1667,'Vålberg'),
    (367,60.1833,18.1833,'Gimo'),
    (368,62.4333,17.3667,'Johannedal'),
    (369,58.0167,11.8333,'Stora Höga'),
    (370,59.6,13.4333,'Deje'),
    (371,56.75,15.2667,'Lessebo'),
    (372,60.8833,16.7167,'Ockelbo'),
    (373,56.3167,12.7667,'Vejbystrand'),
    (374,59.6,16.7,'Irsta'),
    (375,56.6833,12.7333,'Frösakull'),
    (376,56,12.8167,'Bårslöv'),
    (377,59.8,17.7667,'Alsike'),
    (378,56.2833,13.7,'Bjärnum'),
    (379,63.9,20.5667,'Sävar'),
    (380,58.0167,14.4667,'Gränna'),
    (381,62.3,17.3833,'Kvissleby'),
    (382,57.05,12.2667,'Träslövsläge'),
    (383,58.35,11.85,'Herrestad'),
    (384,56.1833,15.85,'Jämjö'),
    (385,58.2167,14.65,'Ödeshög'),
    (386,55.4,12.9167,'Ljunghusen'),
    (387,59.9333,16.8833,'Heby'),
    (388,62.0333,14.35,'Sveg'),
    (389,63.5667,19.5,'Nordmaling'),
    (390,63.6167,19.9167,'Hörnefors'),
    (391,57.05,12.4,'Tvååker'),
    (392,59.4667,15.3667,'Frövi'),
    (393,57.7167,14.1667,'Odensjö'),
    (394,61.35,16.0833,'Alfta'),
    (395,59.6,15.8333,'Kolsva'),
    (396,59.1667,17.8,'Vårsta'),
    (397,64.6167,21.2,'Bureå'),
    (398,62.3333,17.3667,'Stockvik'),
    (399,57.0167,14.9,'Rottne'),
    (400,59.5333,14.2667,'Storfors'),
    (401,64.2,19.7167,'Vindeln'),
    (402,60.05,18.0667,'Alunda'),
    (403,62.5,17.5167,'Söråker'),
    (404,57.55,12.7833,'Fritsla'),
    (405,59.8333,15.6833,'Skinnskatteberg'),
    (406,65.3,21.3833,'Bergsviken'),
    (407,63.3167,14.5,'Krokom'),
    (408,60.2,17.9,'Österbybruk'),
    (409,55.95,12.8167,'Glumslöv'),
    (410,64.8333,20.9833,'Kåge'),
    (411,58.1333,15.6667,'Rimforsa'),
    (412,63.8167,20.1833,'Röbäck'),
    (413,58.3333,15.7333,'Sturefors'),
    (414,59.1667,17.65,'Pershagen'),
    (415,59.8833,12.2833,'Charlottenberg'),
    (416,61.4667,16.3833,'Arbrå'),
    (417,65.1,17.1,'Storuman'),
    (418,56.1,12.85,'Hyllinge'),
    (419,63.7,20.3167,'Obbola'),
    (420,61.8,16.5833,'Delsbo'),
    (421,57.7,11.75,'Andalen'),
    (422,59.6667,16.65,'Tillberga'),
    (423,60.6,16.5167,'Storvik'),
    (424,58.2,12.2167,'Sjuntorp'),
    (425,60.2267,17.7047,'Örbyhus'),
    (426,60.6833,15.1,'Insjön'),
    (427,62.45,17.35,'Sundsbruk'),
    (428,59.4167,13.6333,'Skattkärr'),
    (429,58.3833,15.4333,'Vikingstad'),
    (430,56.35,12.8167,'Förslöv'),
    (431,61.2667,17.1667,'Sandarne'),
    (432,56.55,13.7167,'Strömsnäsbruk'),
    (433,56.0333,14.1667,'Hammar'),
    (434,64.9167,19.4833,'Norsjö'),
    (435,65.1833,18.75,'Malå'),
    (436,56.0667,13.2167,'Ljungbyhed'),
    (437,56.8167,12.7333,'Getinge'),
    (438,57.2667,12.3167,'Veddige'),
    (439,57.3167,12.1667,'Frillesås'),
    (440,57.7,14.4667,'Forserum'),
    (441,58.4667,15.6333,'Ekängen'),
    (442,59.1667,14.8667,'Fjugesta'),
    (443,60.5167,14.2167,'Vansbro'),
    (444,64.2,20.85,'Robertsfors'),
    (445,62.2667,17.3833,'Njurundabommen'),
    (446,66.05,17.8833,'Arjeplog'),
    (447,59.1333,16.7333,'Malmköping'),
    (448,55.4333,13.3333,'Anderslöv'),
    (449,67.1833,23.3667,'Pajala'),
    (450,59.5667,16.25,'Kolbäck'),
    (451,58.8,16.7833,'Stigtomta'),
    (452,60.5,14.9667,'Mockfjärd'),
    (453,59.7,15.1167,'Storå'),
    (454,61.2167,17.1333,'Ljusne'),
    (455,61.2333,14.0333,'Älvdalen'),
    (456,55.6,13.25,'Klågerup'),
    (457,56.4,14.3167,'Lönsboda'),
    (458,58.5667,11.9833,'Färgelanda'),
    (459,56.0667,12.8667,'Mörarp'),
    (460,57.5,14.7,'Bodafors'),
    (461,57.1667,16.0333,'Högsby'),
    (462,56.2333,12.6667,'Jonstorp'),
    (463,63,17.6833,'Bollstabruk'),
    (464,59.6,13.7167,'Molkom'),
    (465,56.1333,13.0667,'Kvidinge'),
    (466,56.4,16,'Torsås'),
    (467,58.1833,12.7167,'Nossebro'),
    (468,58.3833,11.4833,'Brastad'),
    (469,60.7333,15.4333,'Bjursås'),
    (470,60.6833,15.4667,'Grycksbo'),
    (471,57,14.5667,'Moheda'),
    (472,56.7167,12.6667,'Haverdal'),
    (473,58.2333,11.6833,'Henån'),
    (474,56.0333,13.6667,'Sösdala'),
    (475,63.2,18.5,'Bjästa'),
    (476,57.9833,12.2,'Skepplanda'),
    (477,60.7167,16.6,'Järbo'),
    (478,64.15,17.35,'Åsele'),
    (479,58.2744,13.7136,'Stenstorp'),
    (480,59.1167,15.2,'Hällabrottet'),
    (481,56.7,12.7333,'Gullbrandstorp'),
    (482,64.7333,21.0667,'Bergsbyn'),
    (483,56.5167,16.3833,'Mörbylånga'),
    (484,56.7442,12.9444,'Åled'),
    (485,56.7833,14.45,'Vislanda'),
    (486,59.45,16.3167,'Kvicksund'),
    (487,57.3167,13.8667,'Hillerstorp'),
    (488,56.9167,13.9833,'Lagan'),
    (489,57,15.2833,'Lenhovda'),
    (490,57.3167,15.5833,'Virserum'),
    (491,66.3881,23.6536,'Övertorneå'),
    (492,65.4286,21.6892,'Rosvik'),
    (493,58.4417,11.3028,'Hunnebostrand'),
    (494,64.95,21.2,'Byske'),
    (495,60.6167,16.8833,'Forsbacka'),
    (496,58.7236,11.3236,'Tanumshede'),
    (497,58.6667,16.1833,'Jursla'),
    (498,56.05,14.2833,'Fjälkinge'),
    (499,56.2308,15.1208,'Bräkne-Hoby'),
    (500,62.9833,17.8667,'Bjärtrå'),
    (501,56.2667,14.7667,'Svängsta'),
    (502,56.75,14.9167,'Ingelstad'),
    (503,59.5833,17.8833,'Rosersberg'),
    (504,60.45,16.0167,'Långshyttan'),
    (505,56.35,13.65,'Vittsjö'),
    (506,62.75,15.4167,'Bräcke'),
    (507,55.8667,13.0833,'Teckomatorp'),
    (508,60.5667,17.45,'Älvkarleby'),
    (509,63.3333,19.1667,'Husum'),
    (510,55.8833,12.9333,'Asmundtorp'),
    (511,56.6167,12.9333,'Trönninge'),
    (512,59.45,18.2833,'Svinningeudd'),
    (513,56.1083,15.4625,'Hasslö'),
    (514,56.2861,13.9361,'Hästveda'),
    (515,59.3667,17.2,'Stallarholmen'),
    (516,59.3019,14.9494,'Garphyttan'),
    (517,56.5167,12.9333,'Mellbystrand'),
    (518,56.65,16.2667,'Rinkabyholm'),
    (519,64.5167,20.65,'Burträsk'),
    (520,57.4833,11.9833,'Vallda'),
    (521,56.6333,16.1667,'Ljungbyholm'),
    (522,55.8667,13.15,'Marieholm'),
    (523,56.75,12.7167,'Harplinge'),
    (524,60.1833,17.1833,'Östervåla'),
    (525,58.1333,13.3333,'Floby'),
    (526,58.5167,13.3167,'Källby'),
    (527,59.1528,16.4986,'Hälleforsnäs'),
    (528,63.1833,17.0667,'Långsele'),
    (529,61.7333,16.9833,'Sörforsa'),
    (530,60.3833,17.2333,'Söderfors'),
    (531,58.9833,14.1167,'Gullspång'),
    (532,64.8667,20.3833,'Boliden'),
    (533,62.5167,17.3833,'Bergeforsen'),
    (534,60.3333,18.4333,'Öregrund'),
    (535,63.9128,19.8183,'Vännäsby'),
    (536,59.0667,15.3333,'Pålsboda'),
    (537,57.4667,12,'Backa'),
    (538,57.0667,15.05,'Braås'),
    (539,64.2667,16.4167,'Dorotea'),
    (540,58.5667,12.3667,'Brålanda'),
    (541,57.65,12.5,'Rävlanda'),
    (542,56.9833,16.3333,'Blomstermåla'),
    (543,57.7,13.0167,'Gånghester'),
    (544,57.3833,15.8,'Målilla'),
    (545,57.925,12.0869,'Alafors'),
    (546,58.9167,12.3167,'Dals Långed'),
    (547,58.1333,11.8333,'Svanesund'),
    (548,57.5,15,'Ekenässjön'),
    (549,59.0167,16.3833,'Valla'),
    (550,59.8667,16.0333,'Virsbo Bruk'),
    (551,59.3167,18.45,'Mörtnäs'),
    (552,58.5333,15.9667,'Kimstad'),
    (553,56.2333,14.5167,'Jämshög'),
    (554,57.5333,13.35,'Limmared'),
    (555,56.9833,14.3,'Rydaholm'),
    (556,57.5333,12.1,'Anneberg'),
    (557,57.6167,15.5667,'Mariannelund'),
    (558,57.1667,13.7333,'Bredaryd'),
    (559,59.7667,16.2,'Ramnäs'),
    (560,57.15,13.8167,'Forsheda'),
    (561,57.1667,14.5833,'Lammhult'),
    (562,57.8667,14.3,'Kaxholmen'),
    (563,56.0167,13.1,'Kågeröd'),
    (564,58.1167,12.5333,'Sollebrunn'),
    (565,57.9333,11.5833,'Rönnäng'),
    (566,60.0167,17.7333,'Vattholma'),
    (567,57.3667,14.9,'Landsbro'),
    (568,56.7,16.1167,'Trekanten'),
    (569,57.35,14.4667,'Vrigstad'),
    (570,59.7667,12.3667,'Åmotfors'),
    (571,59.9333,16.95,'Morgongåva'),
    (572,63.3472,13.4611,'Järpen'),
    (573,57.6,11.8,'Donsö'),
    (574,61.7167,16.1667,'Järvsö'),
    (575,58.4667,13.8667,'Stöpen'),
    (576,58.7,11.25,'Grebbestad'),
    (577,59.0242,17.5392,'Hölö'),
    (578,59.7333,17.3,'Örsundsbro'),
    (579,59.4333,15.6,'Fellingsbro'),
    (580,56.3,14.1333,'Glimåkra'),
    (581,57.9333,11.85,'Kode'),
    (582,56.05,14.0167,'Önnestad'),
    (583,59.1667,15.5167,'Odensbacken'),
    (584,55.5167,12.9167,'Klagshamn'),
    (585,56.2667,14.2,'Sibbhult'),
    (586,65.4167,21.45,'Norrfjärden'),
    (587,55.4667,13.6,'Rydsgård'),
    (588,57.55,12.4333,'Sätila'),
    (589,55.9167,13.55,'Sätofta'),
    (590,59.25,14.95,'Vintrosa'),
    (591,56.8667,14.6333,'Gemla'),
    (592,55.9833,12.8,'Gantofta'),
    (593,58.3611,11.2319,'Smögen'),
    (594,58.3,13.1833,'Kvänum'),
    (595,57.8833,11.5833,'Marstrand'),
    (596,58.0333,11.7667,'Höviksnäs'),
    (597,57.45,13.5833,'Hestra'),
    (598,57.6167,11.7833,'Styrsö'),
    (599,61.8,15.85,'Färila'),
    (600,55.8333,14.0833,'Degeberga'),
    (601,58.85,14.2167,'Hova'),
    (602,57.1833,13.5833,'Reftele'),
    (603,58.4897,15.5306,'Berg'),
    (604,60.55,15.1333,'Djurås'),
    (605,55.3451,13.3894,'Smygehamn'),
    (606,65.5333,17.5333,'Sorsele'),
    (607,56.9667,16.4333,'Timmernabben'),
    (608,59.2583,15.3333,'Ekeby'),
    (609,59.2833,16.6667,'Ärla'),
    (610,60.7833,14.85,'Siljansnäs'),
    (611,61.9833,17.0667,'Bergsjö'),
    (612,58.0333,12.1583,'Lödöse'),
    (613,55.9833,14.15,'Norra Åsum'),
    (614,56.1,15.6667,'Sturkö'),
    (615,57.35,12.4667,'Horred'),
    (616,55.4233,13.57,'Skivarp'),
    (617,57.1167,14.1667,'Bor'),
    (618,62.5,16.15,'Fränsta'),
    (619,56.05,14.6833,'Mjällby'),
    (620,57.6833,16.3167,'Ankarsrum'),
    (621,56.15,14.1167,'Hanaskog'),
    (622,60.75,15.9167,'Svärdsjö'),
    (623,59.5081,11.8442,'Töcksfors'),
    (624,57.9167,12.0167,'Diseröd'),
    (625,65.35,21.2,'Roknäs'),
    (626,63.1667,18.5667,'Köpmanholmen'),
    (627,63.8333,20.4833,'Täfteå'),
    (628,58.05,11.7333,'Myggenäs'),
    (629,60.15,16.9333,'Tärnsjö'),
    (630,55.7667,13.1667,'Stångby'),
    (631,55.5833,13.6,'Blentarp'),
    (632,57.1333,15.1667,'Klavreström'),
    (633,57.9833,16.3167,'Överum'),
    (634,59.1333,18.4,'Dalarö'),
    (635,56.55,13.0667,'Veinge'),
    (636,56.7833,12.6667,'Steninge'),
    (637,58.3833,13.5667,'Axvall'),
    (638,63.45,18.1,'Bredbyn'),
    (639,56.9578,13.0809,'Torup'),
    (640,57.75,15.8,'Södra Vi'),
    (641,59.2256,15.1592,'Marieberg'),
    (642,58.2667,16.4833,'Gusum'),
    (643,60.9092,15.0208,'Vikarbyn'),
    (644,59,15.05,'Åsbro'),
    (645,59.55,16.5,'Barkarö'),
    (646,60.5167,17.6167,'Karlholmsbruk'),
    (647,55.4667,13.9333,'Köpingebro'),
    (648,58.9833,12.25,'Billingsfors'),
    (649,59.1333,15.3167,'Sköllersta'),
    (650,55.7833,13.5,'Löberöd'),
    (651,61.2333,16.5667,'Kilafors'),
    (652,55.9,13.3833,'Stehag'),
    (653,56.1667,14.4833,'Näsum'),
    (654,60.3,16.4167,'Horndal'),
    (655,57.6,13.2333,'Länghem'),
    (656,56,13.2833,'Röstånga'),
    (657,55.4,13.0667,'Skegrie'),
    (658,55.4833,13.0167,'Hököpinge'),
    (659,55.4789,13.5978,'Villie'),
    (660,59.25,14.25,'Björneborg'),
    (661,65.9167,22.65,'Töre'),
    (662,59.9667,18.8,'Älmsta'),
    (663,56.4167,12.6333,'Torekov'),
    (664,56.5167,13.3167,'Knäred'),
    (665,56.15,12.8167,'Strövelstorp'),
    (666,65.2833,21.3833,'Hortlax'),
    (667,58.5,14.4,'Mölltorp'),
    (668,57.1667,16.2833,'Påskallavik'),
    (669,58.7333,16.8667,'Svalsta'),
    (670,56.2833,13.9167,'Sjörröd'),
    (671,60.1667,13.5,'Ekshärad'),
    (672,58.45,13.2667,'Vinninga'),
    (673,62.2833,17.3833,'Skottsund'),
    (674,60.5167,15.5333,'Ornäs'),
    (675,56.8647,12.5658,'Skrea'),
    (676,59.9833,14.1833,'Lesjöfors'),
    (677,65.85,23.1167,'Rolfs'),
    (678,57.8833,12.4833,'Västra Bodarne'),
    (679,63.1,16.35,'Hammarstrand'),
    (680,59.1833,17.6333,'Mölnbo'),
    (681,56.1167,13.7333,'Tormestorp'),
    (682,59.95,17.5833,'Lövstalöt'),
    (683,55.9,13.6,'Ludvigsborg'),
    (684,63.3167,14.8167,'Lit'),
    (685,59.3333,13.1,'Slottsbron'),
    (686,60.6,16.6167,'Kungsgården'),
    (687,57.5833,14.4667,'Malmbäck'),
    (688,60.4,17,'Hedesunda'),
    (689,56.0667,14.0833,'Färlöv'),
    (690,62.0667,17.2833,'Gnarp'),
    (691,57.7833,11.8333,'Nolvik'),
    (692,57.65,12.8833,'Rydboholm'),
    (693,59.4333,13.1833,'Edsvalla'),
    (694,59.5667,16.5333,'Enhagen-Ekbacken'),
    (695,60.5333,16.7333,'Årsunda'),
    (696,59.5667,16.3667,'Dingtuna'),
    (697,62.7667,14.45,'Svenstavik'),
    (698,59.6256,16.9419,'Hummelsta'),
    (699,66.35,22.8833,'Överkalix'),
    (700,63.9303,19.2136,'Bjurholm'),
    (701,57.8333,15.2667,'Österbymo'),
    (702,59.3607,17.9717,'Sundbyberg'),
    (703,59.3667,18.0167,'Solna'),
    (704,55.7,13.1833,'Lund'),
    (705,59.4167,17.8333,'Jakobsberg'),
    (706,59.4667,17.9,'Sollentuna'),
    (707,59.31,18.1639,'Nacka'),
    (708,59.4,18.0833,'Djursholm'),
    (709,59.2333,17.9833,'Huddinge'),
    (710,59.2333,18.3,'Tyresö'),
    (711,57.7333,12.1167,'Partille'),
    (712,56.25,12.85,'Ängelholm'),
    (713,59.2,17.7333,'Rönninge'),
    (714,57.1833,14.0333,'Värnamo'),
    (715,57.3667,13.7333,'Gnosjö'),
    (716,60.7333,15.0167,'Leksand'),
    (717,59.1982,12.3743,'Skogen');

DROP TABLE IF EXISTS `city_seq`;
CREATE TABLE `city_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `city_seq` WRITE;
INSERT INTO `city_seq` VALUES (1251);
UNLOCK TABLES;