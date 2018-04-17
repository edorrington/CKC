CREATE TABLE telephone_numbers (
    id SERIAL PRIMARY KEY,
    family_id INTEGER NOT NULL REFERENCES families(id),
    telephone_number text,
    telephone_type TEXT NOT NULL REFERENCES telephone_types(name)
);
