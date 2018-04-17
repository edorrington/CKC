CREATE TABLE users
(id VARCHAR(255) PRIMARY KEY,
 first_name VARCHAR(255),
 last_name VARCHAR(255),
 email VARCHAR(255),
 last_login TIMESTAMP,
 is_active BOOLEAN DEFAULT TRUE,
 pass VARCHAR(100),
 role VARCHAR(24),
 site_id INTEGER not null REFERENCES sites(id));

-- Initial users of the system. All have the password 'password'
insert into users (id, first_name, last_name, email, role, pass, site_id) values
    ('ed','Edward','Dorrington','ed@dorrington.org','admin','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',0),
    ('kristina','Kristina','Bedrossian','kristina@brightresearchgroup.com','evaluator','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',0),
    ('brightstar','Brightstar','Ohlson','bohlson@brightresearchgroup.com','evaluator','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',0),
    ('daniel','Daniel','Donnelly','ddonnelly@eastbayinnovations.org','admin','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',0),
    ('randy','Randy','Nakamura','rnakamura@husd.k12.ca.us','site-admin','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',2),
    ('nora','Nora','Juarez-Fregoso','njuarez-fregoso@husd.k12.ca.us','site-user','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',2),
    ('jessica','Jessica','Woodward','jessica.woodward@acgov.org','user','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',0),
    ('gloria','Gloria','Vargas','gloriavargas@sanleandro.k12.ca.us','site-admin','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',3),
    ('eliza','Eliza','Schiffrin','eliza.schiffrin@ousd.k12.ca.us','site-admin','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi',1),
    ('john','John','Hamilton','john@noeticlogic.com','health-clerk','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi', 1),
    ('stephanie','Stephanie','Williams','steph@noeticlogic.com','health-clerk','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi', 2),
    ('bill','Bill','Jones','bill@noeticlogic.com','health-clerk','$2a$10$K3gPVu0G9hEds4kHahSgdOR7eewBmXeE07vUZAPV5Q8coTqtX2YIi', 3);

