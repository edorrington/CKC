CREATE TABLE reminder_types (name TEXT NOT NULL PRIMARY KEY);
INSERT INTO reminder_types (name) VALUES ('mail'), ('email'), ('sms');

CREATE TABLE reminder_schedules (
    type TEXT NOT NULL REFERENCES reminder_types(name),
    site_id INT NOT NULL REFERENCES sites(id),
    days INT NOT NULL DEFAULT 0,
    hours INT NOT NULL DEFAULT 0,
    PRIMARY KEY (type, site_id)
);
INSERT INTO reminder_schedules (type, site_id, days, hours)
VALUES
('mail',1,3,0), ('mail',2,3,0), ('mail',3,3,0),
('email',1,1,0), ('email',2,1,0), ('email',3,1,0),
('sms',1,0,1), ('sms',2,0,1), ('sms',3,0,1);

CREATE TABLE reminder_templates (
    id SERIAL PRIMARY KEY,
    site_id INT NOT NULL REFERENCES sites(id),
    type TEXT NOT NULL REFERENCES reminder_types(name),
    language TEXT NOT NULL DEFAULT 'english',
    content TEXT,
    CONSTRAINT nodups UNIQUE (site_id, type, language)
);
