CREATE TABLE appointment_types
(name TEXT PRIMARY KEY, position INT NOT NULL DEFAULT 0);
INSERT INTO appointment_types (name, position) values ('ET', 1), ('HIT', 2), ('CEC - EBAC', 3), ('CEC - Other', 4), ('Program Staff', 5), ('Other', 10);

CREATE TABLE appointments (
    id SERIAL PRIMARY KEY,
    title TEXT,
    location TEXT,
    description TEXT,
    site_id INTEGER NOT NULL REFERENCES sites(id),
    case_id INTEGER NOT NULL REFERENCES cases(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    type TEXT NOT NULL REFERENCES appointment_types(name),
    r_email BOOLEAN DEFAULT false,
    r_mail BOOLEAN DEFAULT false,
    r_sms BOOLEAN DEFAULT false,
    s_email BOOLEAN DEFAULT false,
    s_mail BOOLEAN DEFAULT false,
    s_sms BOOLEAN DEFAULT false);
