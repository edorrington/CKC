CREATE TABLE schools (
    id SERIAL PRIMARY KEY,
    name text,
    school_type_id INTEGER NOT NULL REFERENCES school_types(id),
    site_id INTEGER NOT NULL REFERENCES sites(id)
);
INSERT INTO schools (name, school_type_id, site_id) VALUES
-- Oakland
('165 • Acorn Woodland', 1, 1),
('101 • Allendale', 1, 1),
('102 • Bella Vista', 1, 1),
('178 • Bridges Academy', 1, 1),
('103 • Brookfield', 1, 1),
('105 • Burckhalter', 1, 1),
('168 • Carl Munck', 1, 1),
('106 • Chabot', 1, 1),
('108 • Cleveland', 1, 1),
('149 • Community United', 1, 1),
('111 • Crocker Highlands', 1, 1),
('107 • East Oakland Pride', 1, 1),
('115 • Emerson', 1, 1),
('181 • Encompass Academy', 1, 1),
('177 • Esperanza', 1, 1),
('116 • Franklin', 1, 1),
('172 • Fred T. Korematsu Discovery Academy', 1, 1),
('117 • Fruitvale', 1, 1),
('123 • Futures', 1, 1),
('118 • Garfield', 1, 1),
('119 • Glenview', 1, 1),
('114 • Global Family', 1, 1),
('122 • Grass Valley', 1, 1),
('112 • Greenleaf', 1, 1),
('127 • Hillcrest', 1, 1),
('170 • Hoover', 1, 1),
('136 • Horace Mann', 1, 1),
('166 • Howard', 1, 1),
('186 • International Community School', 1, 1),
('142 • Joaquin Miller', 1, 1),
('171 • Kaiser', 1, 1),
('121 • La Escuelita', 1, 1),
('129 • Lafayette', 1, 1),
('131 • Laurel', 1, 1),
('133 • Lincoln', 1, 1),
('154 • Madison Park Lower Campus, formerly Sobrante Park', 1, 1),
('179 • Manzanita Community School', 1, 1),
('175 • Manzanita Seed', 1, 1),
('138 • Markham', 1, 1),
('182 • Martin Luther King, Jr.', 1, 1),
('235 • Melrose Leadership', 1, 1),
('143 • Montclair', 1, 1),
('125 • New Highland Academy', 1, 1),
('144 • Parker', 1, 1),
('145 • Peralta', 1, 1),
('146 • Piedmont Avenue', 1, 1),
('183 • Place @ Prescott', 1, 1),
('193 • Reach Academy', 1, 1),
('148 • Redwood Heights', 1, 1),
('192 • Rise', 1, 1),
('191 • Sankofa', 1, 1),
('151 • Sequoia', 1, 1),
('190 • Think College Now', 1, 1),
('157 • Thornhill', 1, 1),
('224 • Alliance Academy', 2, 1),
('206 • Bret Harte', 2, 1),
('201 • Claremont', 2, 1),
('210 • Edna Brewer', 2, 1),
('221 • Elmhurst Community Prep', 2, 1),
('203 • Frick', 2, 1),
('211 • Montera', 2, 1),
('212 • Roosevelt', 2, 1),
('226 • Roots International', 2, 1),
('228 • United For Success', 2, 1),
('236 • Urban Promise Academy', 2, 1),
('213 • Westlake', 2, 1),
('204 • West Oakland Middle School', 2, 1),
('301 • Castlemont High School', 3, 1),
('232 • Coliseum College Prep', 3, 1),
('310 • Dewey Academy', 3, 1),
('302 • Fremont High School', 3, 1),
('335 • Life Academy', 3, 1),
('215 • Madison Park Upper Campus, formerly Madison', 3, 1),
('303 • Mcclymonds High School', 3, 1),
('338 • Metwest', 3, 1),
('304 • Oakland High', 3, 1),
('353 • Oakland International High', 3, 1),
('305 • Oakland Technical', 3, 1),
('352 • Rudsdale Continuation School', 3, 1),
('306 • Skyline', 3, 1),
('330 • Sojourner Truth', 3, 1),
('313 • Street Academy', 3, 1),
('185 • Ascend', 4, 1),
('113 • Learning Without Limits', 4, 1),
('333 • Community Day School', 5, 1),
('311 • Gateway To College at Laney College', 5, 1),
-- Hayward
('Bowman', 1, 2),
('Burbank', 1, 2),
('Cherryland', 1, 2),
('East Avenue', 1, 2),
('Eden Gardens', 1, 2),
('Eldridge', 1, 2),
('Fairview', 1, 2),
('Faith Ringgold', 1, 2),
('Glassbrook', 1, 2),
('Harder', 1, 2),
('Longwood', 1, 2),
('Lorin Eden', 1, 2),
('Palma Ceia', 1, 2),
('Park', 1, 2),
('Ruus', 1, 2),
('Schafer Park', 1, 2),
('Southgate', 1, 2),
('Stonebrae', 1, 2),
('Strobridge', 1, 2),
('Treeview', 1, 2),
('Treeview (Bidwell-Campus)', 1, 2),
('Tyrrell', 1, 2),
('Anthony Ochoa', 2, 2),
('Bret Harte', 2, 2),
('Cesar Chavez', 2, 2),
('Martin Luther King, Jr.', 2, 2),
('Winton', 2, 2),
('Brenkwitz Continuation High School', 3, 2),
('Hayward High School', 3, 2),
('Mt. Eden High School', 3, 2),
('Tennyson High School', 3, 2),
-- San Leandro
('Garfield Elementary', 1, 3),
('Jefferson Elementary', 1, 3),
('Madison Elementary', 1, 3),
('McKinley Elementary', 1, 3),
('Monroe Elementary', 1, 3),
('Roosevelt Elementary', 1, 3),
('Washington Elementary', 1, 3),
('Wilson Elementary', 1, 3),
('Bancroft Middle School', 2, 3),
('John Muir Middle School', 2, 3),
('Lincoln High/Lighthouse Independent Study Center', 3, 3),
('San Leandro High & Korematsu Campus', 3, 3),
('San Leandro Adult School', 6, 3);
