CREATE TABLE coverage_types (name text primary key, position int default 0);
INSERT INTO coverage_types (name, position) VALUES ('HealthPac', 1), ('Medi-Cal', 2), ('CalFresh', 3), ('Kaiser CHP', 4), ('Covered California', 5), ('CalWorks', 6), ('WIC', 7), ('Other', 20);
