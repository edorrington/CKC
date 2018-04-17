CREATE TABLE outreach_event_types (
    name TEXT PRIMARY KEY,
    position INTEGER NOT NULL DEFAULT 0);
INSERT INTO outreach_event_types (name, position)
VALUES ('Parent meeting/café', 10), ('Open House', 20), ('CFRC Coordinator training', 30), ('School-hosted event', 40), ('CFRC/Hub-hosted enrollment event', 50), ('Other', 100);

CREATE TABLE outreach_events (
    id SERIAL PRIMARY KEY,
    title TEXT,
    location TEXT,
    description TEXT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    site_id INTEGER NOT NULL REFERENCES sites(id),
    type TEXT NOT NULL REFERENCES outreach_event_types(name),
    enrollment_assistance BOOLEAN NOT NULL DEFAULT false,
    attendance INTEGER);
