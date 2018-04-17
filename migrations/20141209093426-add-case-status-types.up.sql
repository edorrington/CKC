CREATE TABLE case_status_types (
    name text primary key,
    position int default 0,
    is_open boolean not null default true
);

INSERT INTO case_status_types (position, name, is_open)
VALUES  (0, 'Initial', true),
        (1, 'Open', true),
        (2, 'Appointment Scheduled', true),
        (3, 'Appointment Missed', true),
        (4, 'Appointment Rescheduled', true),
        (5, 'Appointment Cancelled', true),
        (6, 'Provided Website', false),
        (7, 'Case Assist', false),
        (8, 'Referred To Another Clinic', false),
        (9, 'Already Insured', false),
        (10, 'Does Not Qualify', false),
        (11, 'Unreachable', true),
        (12, 'Does Not Want', false),
        (13, 'Closed', false);
