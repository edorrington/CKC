CREATE TABLE languages_spoken (name text primary key, display_name text, locale text, position int default 0);
INSERT INTO languages_spoken (name, display_name, locale, position)
VALUES
('arabic', 'Arabic', 'ar', 1),
('armenian', 'Armenian', 'hy', 2),
('cantonese', 'Cantonese', 'zh', 3),
('english', 'English', 'en_US', 4),
('farsi', 'Farsi', 'fa', 5),
('hindi', 'Hindi', 'hi_IN', 6),
('hmong', 'Hmong', 'hmn', 7),
('khmer', 'Khmer', 'km', 8),
('korean', 'Korean', 'ko', 9),
('mandarin', 'Mandarin', 'zh', 10),
('russian', 'Russian', 'ru', 11),
('spanish', 'Spanish', 'es_US', 12),
('tagalog', 'Tagalog', 'tl', 13),
('vietnamese', 'Vietnamese', 'vi_VN', 14),
('asl', 'ASL', '', 15),
('other', 'Other Language', '', 100);
