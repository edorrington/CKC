CREATE TABLE email_address_types (name text primary key, position int default 0);
INSERT INTO email_address_types (name, position) VALUES ('Home', 1), ('Work', 2), ('Other', 3);
