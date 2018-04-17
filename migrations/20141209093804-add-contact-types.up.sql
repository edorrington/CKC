CREATE TABLE contact_type_categories (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);
INSERT INTO contact_type_categories (id, name) VALUES
(1,'Outreach Call → Spoke with family'),
(2,'Outreach Call → Left message'),
(3,'Outreach Call → Unable to reach'),
(4,'Incoming Call/ Drop-In → Spoke with family'),
(5,'Incoming Call/ Drop-In → Referred for Other Services'),
(6,'Appointment → Appointment Attended'),
(7,'Appointment → Appointment Not Attended'),
(8,'Appointment → Referred for Other Services');

CREATE TABLE contact_types (
    id INTEGER PRIMARY KEY,
    contact_type_category_id INTEGER NOT NULL REFERENCES contact_type_categories(id),
    name TEXT NOT NULL,
    appointment BOOLEAN NOT NULL DEFAULT false,
    suggested_case_status TEXT REFERENCES case_status_types(name)
);

INSERT INTO contact_types (id, contact_type_category_id, name) VALUES
(1, 1, 'Scheduled appointment', 'Appointment Scheduled'),
(2, 1, 'Scheduled renewal appointment', 'Appointment Scheduled'),
(3, 1, 'Provided website or mailed additional info', 'Provided Website'),
(4, 1, 'Screened and do not qualify', 'Does Not Qualify'),
(5, 1, 'Already have insurance', 'Already Insured'),
(6, 1, 'Do not want our service', 'Does Not Want'),
(7, 1, 'Asked to call later', null),
(8, 1, 'Referred to another clinic', 'Referred To Another Clinic'),
(9, 2, 'Voicemail', null),
(10, 2, 'Family member/friend', null),
(11, 3, 'Number disconnected or wrong number', 'Unreachable'),
(12, 3, 'Voicemail full', 'Unreachable'),
(13, 3, 'No Answer/line keeps ringing', 'Unreachable'),
(14, 4, 'Scheduled appointment', 'Appointment Scheduled'),
(15, 4, 'Scheduled renewal appointment', 'Appointment Scheduled'),
(16, 4, 'Provided website or mailed additional info', 'Provided Website'),
(17, 4, 'Screened and do not qualify', 'Does Not Qualify'),
(18, 4, 'Already have insurance', 'Already Insured'),
(19, 4, 'Do not want our service', 'Does Not Want'),
(20, 4, 'Asked to call later', null),
(21, 4, 'Referred to another clinic', 'Referred To Another Clinic'),
(22, 5, 'Health-related', 'Case Assist'),
(23, 5, 'Legal services', 'Case Assist'),
(24, 5, 'Food, housing, and other basic needs', 'Case Assist'),
(25, 5, 'Parenting, childcare, and help with homework', 'Case Assist'),
(26, 5, 'Safety in the home and community', 'Case Assist'),
(27, 5, 'School-related issues', 'Case Assist'),
(28, 5, 'Activities in the community', 'Case Assist'),
(29, 5, 'Other', 'Case Assist'),
(30, 6, 'New Application Submitted', 'Closed'),
(31, 6, 'Renewal Application Submitted', 'Closed'),
(32, 6, 'Case Assist', null),
(33, 7, 'Missed', 'Open'),
(34, 7, 'Cancelled', 'Open'),
(35, 7, 'Rescheduled', 'Appointment Scheduled'),
(36, 8, 'Health-related', 'Case Assist'),
(37, 8, 'Legal services', 'Case Assist'),
(38, 8, 'Food, housing, and other basic needs', 'Case Assist'),
(39, 8, 'Parenting, childcare, and help with homework', 'Case Assist'),
(40, 8, 'Safety in the home and community', 'Case Assist'),
(41, 8, 'School-related issues', 'Case Assist'),
(42, 8, 'Activities in the community', 'Case Assist'),
(43, 8, 'Other', 'Case Assist');

UPDATE contact_types SET appointment = true WHERE id IN (1, 2, 14, 15, 35);
