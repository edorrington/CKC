-- name: get-languages
-- Returns a list of all languages
select name, display_name, locale from languages_spoken order by position asc

-- name: get-locale-for
-- Returns the locale for a given language
select locale from languages_spoken where name = :name limit 1

-- name: get-template-languages
-- Returns a list of all languages for templates (i.e. it removes ASL)
select name, display_name from languages_spoken where name <> 'asl' and name <> 'other' order by position asc

-- name: get-ethnicities
-- Returns a list of all ethnicities
select name, display_name from ethnicities order by position asc

-- name: ethnicity-name
-- Returns the display name of a given ethnicity
select display_name from ethnicities where name = :name

-- name: language-name
-- Returns the display name of a given language
select display_name from languages_spoken where name = :name

-- name: get-telephone-types
-- Returns a list of all telephone number types
select name from telephone_types order by position asc

-- name: get-email-address-types
-- Returns a list of all email address types
select name from email_address_types order by position asc

-- name: get-services-provided-types
-- Returns a list of all types of Services Provided for a given section
select name
from services_provided_types spt
where spt.services_provided_section_id = :section_id
order by position asc

-- name: get-coverage-types
-- Returns a list of all the different types of Coverage available
select name from coverage_types order by position asc

-- name: get-case-status-types
-- Returns a list of all possible statuses for a Case
select name from case_status_types where position > 0 order by position asc

-- name: get-member-types
-- Returns a list of the different member types
select name, display_name from member_types order by position asc;

-- name: get-assisters
-- Returns a list of assister types
select * from appointment_types order by position

-- name: get-event-types
-- Returns a list of event types
select * from event_types order by position

-- name: get-outreach-event-types
-- Returns a list of outreach event types
select * from outreach_event_types order by position
