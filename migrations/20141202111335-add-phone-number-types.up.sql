CREATE TABLE telephone_types (name text PRIMARY KEY, position int default 0);
INSERT INTO telephone_types (name, position) values ('Home', 1), ('Work', 2), ('Cell', 3), ('Other', 4), ('Out of Service', 5);
