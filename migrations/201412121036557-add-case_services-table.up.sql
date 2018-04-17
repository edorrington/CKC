CREATE TABLE application_statuses (
    name TEXT PRIMARY KEY
);
INSERT INTO application_statuses (name) VALUES ('Pending'), ('Approved'), ('Denied');

CREATE TABLE case_services (
    id SERIAL PRIMARY KEY,
    case_contacts_id INTEGER NOT NULL REFERENCES case_contacts(id),
    member_id INTEGER NOT NULL REFERENCES members(id),
    service_provided TEXT NOT NULL REFERENCES services_provided_types(name),
    coverage_type TEXT REFERENCES coverage_types(name),
    application_status TEXT NOT NULL REFERENCES application_statuses(name),
    notes TEXT
);
