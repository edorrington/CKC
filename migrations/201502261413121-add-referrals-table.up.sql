CREATE TABLE referrals (
    id SERIAL PRIMARY KEY,
    site_id INTEGER NOT NULL REFERENCES sites(id),
    identified_need TEXT NOT NULL REFERENCES services_provided_types(name),
    service_provided TEXT NOT NULL,
    referral_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT current_timestamp
);
