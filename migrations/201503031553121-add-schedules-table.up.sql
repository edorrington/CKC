CREATE TYPE repeater AS ENUM ('None','Daily','Weekly','Monthly');
CREATE TABLE schedules (
    id SERIAL PRIMARY KEY,
    site_id INTEGER NOT NULL REFERENCES sites(id),
    type TEXT NOT NULL REFERENCES appointment_types(name),
    repeats repeater DEFAULT 'None',
    repeat_begin DATE NOT NULL DEFAULT current_date,
    repeat_end DATE NOT NULL DEFAULT current_date
);

CREATE TABLE schedule_items (
    id SERIAL PRIMARY KEY,
    schedule_id INTEGER NOT NULL REFERENCES schedules(id),
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    slots INTEGER NOT NULL DEFAULT 1
);
