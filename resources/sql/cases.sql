-- name: get-cases
-- Returns a list of all open cases for a site
select c.id, c.status, c.referred_from, c.family_id,
       f.street1, f.street2, f.city, f.zipcode
from cases c, families f, case_status_types cs
where c.family_id = f.id
  and f.site_id = :site_id
  and c.status = cs.name
  and cs.is_open = true
  and cs.position > 0
  and c.deleted = false

-- name: get-all-cases
-- Returns a list of all open cases
select c.id, c.status, c.referred_from, c.family_id,
       f.street1, f.street2, f.city, f.zipcode, f.site_id, s.name as site_name
from cases c, families f, case_status_types cs, sites s
where c.family_id = f.id
  and c.status = cs.name
  and s.id = f.site_id
  and cs.is_open = true
  and cs.position > 0
  and c.deleted = false

-- name: get-case
-- Returns a specific case based on id
select * from cases where id = :id and deleted = false

-- name: delete-case!
-- Deletes the case with a given ID by setting that records deleted flag to true
update cases set deleted = true where id = :id

-- name: current-case-for
-- Returns the current case for the family with the given family_id and site_id
select c.*
from cases c, case_status_types cs, families f
where c.status = cs.name
and c.family_id = f.id
and f.id = :family_id
and f.site_id = :site_id
and cs.position > 0
and c.deleted = false
order by c.id desc
limit 1

-- name: case-statuses
-- Returns a list of all case status types
select name from case_status_types where name <> 'Initial' order by position;

-- name: update-case-status!
-- Updates the status of a given case
update cases set status = :status where id = :id

-- name: update-case-status-and-ref!
-- Updates the status and referred_from fields of a case
update cases
set (status, referred_from) = (:status, :referred_from)
where id = :id

-- name: create-case<!
-- Creates a new blank case record for a given family
insert into cases (family_id, status) values (:family_id, :status)

-- name: get-case-ids-for-family
-- Returns a list of case id's for a given family and site
select c.id
from cases c, families f
where c.family_id = f.id
and f.id = :family_id
and f.site_id = :site_id

-- name: update-case-consent!
-- Updates the consent_signed field of the family record for the given case to the given value
update families set consent_signed = :signed where id = (select family_id from cases c where c.id = :id)

