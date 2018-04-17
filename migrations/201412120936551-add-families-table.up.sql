CREATE TABLE families (
    id SERIAL PRIMARY KEY,
    language text NOT NULL REFERENCES languages_spoken(name) DEFAULT 'english',
    ethnicity text NOT NULL REFERENCES ethnicities(name) DEFAULT 'caucasian',
    monthly_income INTEGER,
    street1 text,
    street2 text,
    city text,
    zipcode text,
    site_id integer NOT NULL REFERENCES sites(id),
    consent_signed boolean default false,
    notes text,
    deleted boolean default false
);
