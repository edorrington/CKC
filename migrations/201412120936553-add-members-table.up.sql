CREATE TABLE members (
    id SERIAL PRIMARY KEY,
    name TEXT,
    sortname TEXT,
    dob DATE,
    member_type TEXT NOT NULL DEFAULT 'child' REFERENCES member_types(name),
    family_id INTEGER NOT NULL REFERENCES families(id),
    school TEXT,
    deleted boolean default false
);
