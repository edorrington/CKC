-- name: get-contacts-for-case
-- Returns a list of all contacts for this case
select * from case_contacts where case_id = :case_id order by contacted_at asc

-- name: get-contacts-for-cases
-- Returns all case_contact records where case_id is in the passed list
select * from case_contacts where case_id in (:case_ids) order by case_id, contacted_at

-- name: get-contacts-for-family
-- Returns all case_contact records for a given family in a given site
select cc.*
from case_contacts cc, cases c, families f
where cc.case_id = c.id
and c.family_id = f.id
and f.id = :family_id
and f.site_id = :site_id
order by cc.case_id, cc.contacted_at

-- name: new-contact<!
-- Creates a contact record with a subset of fields
insert into case_contacts (case_id, user_id, contact_type_id, notes)
values (:case_id, :user_id, :contact_type_id, :notes)

-- name: get-contact-types
-- Returns a list of all contact types
select * from contact_types order by id;

-- name: get-contact-types-for-category
-- Returns a list of all contact types for a given category_id
select *
from contact_types ct
where contact_type_category_id = :id
order by id;

-- name: get-contact-type-categories
-- Returns a list of contact type categories
select * from contact_type_categories order by id;
