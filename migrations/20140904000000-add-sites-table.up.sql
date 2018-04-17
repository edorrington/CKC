CREATE TABLE sites
(id SERIAL PRIMARY KEY,
 name VARCHAR(255),
 phone VARCHAR(255),
 street1 VARCHAR(255),
 street2 VARCHAR(255),
 city VARCHAR(255),
 zip VARCHAR(10),
 is_active BOOLEAN DEFAULT TRUE);

-- The three core sites
insert into sites (id, name, phone, street1, city, zip) values
(0, '<Global>',null, null, null, null)
(1, 'Oakland','(510) 555-1212','555 12th Street','Oakland','94600'),
(2, 'Hayward','(510) 432-4897','8954 7th Avenue','Hayward','94545'),
(3, 'San Leandro','(510) 203-5653','289 Baker Street','San Leandro','94578');

-- Disable the global site
update sites set is_active = false where id = 0;

-- Update the sequence
ALTER SEQUENCE sites_id_seq START WITH 4;
