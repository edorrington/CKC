CREATE TABLE case_contacts (
    id SERIAL PRIMARY KEY,
    case_id INT NOT NULL REFERENCES cases(id),
    user_id TEXT NOT NULL REFERENCES users(id),
    contact_type_id INT NOT NULL REFERENCES contact_types(id),
    contacted_at TIMESTAMP NOT NULL DEFAULT now(),
    notes TEXT
);
