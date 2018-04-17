-- name: services-for-case
-- Returns all services provided for each family member with a given case id
select cs.member_id, m.name, m.member_type, cs.service_provided, cs.coverage_type
from case_services cs, cases c, members m
where cs.case_id = c.id
and c.id = :case_id
and c.deleted = false
and cs.member_id = m.id
and m.deleted = false
order by m.id

-- name: services-for-case-and-member
-- Returns services provided for a family member within a case
select cs.service_provided, cs.coverage_type
from case_services cs, cases c, members m
where cs.case_id = c.id
where c.id = :case_id
and c.deleted = false
and cs.member_id = m.id
and m.id = :member_id
and m.deleted = false

-- name: members-of-type-for-case
-- Returns a list of member id's of a given type who received services in a given case
select cs.member_id, m.name
from case_services cs, members m
where cs.case_id = :case_id
and m.deleted = false
and m.member_type = :member_type
and cs.member_id = m.id

-- name: services-for-contact
-- Returns a list of case services provided for a given case_contact
select m.name, cs.service_provided, cs.coverage_type, cs.notes
from members m, case_services cs
where m.id = cs.member_id
and m.deleted = false
and cs.case_contacts_id = :case_contacts_id;

-- name: create-case-service<!
-- Creates a new case services record
insert into case_services (case_contacts_id, member_id, service_provided, coverage_type, notes)
values (:case_contacts_id, :member_id, :service_provided, :coverage_type, :notes)
