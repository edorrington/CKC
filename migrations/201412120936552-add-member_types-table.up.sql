CREATE TABLE member_types (
    name TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    position integer default 0
);
INSERT INTO member_types (name, display_name, position) VALUES ('guardian','Parent/Guardian',1), ('child','Child',2), ('other','Other',3);
