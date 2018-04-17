CREATE TABLE services_provided_sections (id int primary key, name text not null);
INSERT INTO services_provided_sections (id, name) VALUES (1,'Applications'), (2, 'Referrals');
CREATE TABLE services_provided_types (
    name text primary key,
    services_provided_section_id int not null references services_provided_sections(id),
    position int default 0
);
INSERT INTO services_provided_types (name, services_provided_section_id, position)
VALUES  ('Informational', 1, 10),
        ('Modification', 1, 20),
        ('New Application', 1, 30),
        ('Renewal Application', 1, 40),
        ('Troubleshooting', 1, 50),
        ('SAR-7', 1, 60),
        ('MAGI', 1, 70),
        ('Non-MAGI', 1, 80),
        ('Connect to health home or provider', 2, 90),
        ('Other health-related', 2, 100),
        ('Legal services', 2, 110),
        ('Food, housing, and other basic needs', 2, 120),
        ('Parenting, childcare, and help with homework', 2, 130),
        ('Safety in the home and community', 2, 140),
        ('School-related issues', 2, 150),
        ('Activities in the community', 2, 160);
