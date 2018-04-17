CREATE TABLE email_addresses (
    id SERIAL PRIMARY KEY,
    family_id INTEGER NOT NULL REFERENCES families(id),
    email_address text,
    email_address_type TEXT NOT NULL REFERENCES email_address_types(name)
);
