CREATE TABLE cases (
    id SERIAL PRIMARY KEY,
    family_id INTEGER NOT NULL REFERENCES families(id),
    status TEXT NOT NULL REFERENCES case_status_types(name),
    referred_from TEXT,
    deleted boolean default false);
