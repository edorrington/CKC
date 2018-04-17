-- name: guardians-for
-- Returns the guardians for a given family
select name from members where family_id = :family_id and member_type = 'guardian' and deleted = false

-- name: sort-for
-- Returns the sort order for a given family
select sortname
from members
where family_id = :family_id
and member_type = 'guardian'
and deleted = false
order by sortname
limit 1

-- name: guardians-for-case
-- Returns the guardians for a given case
select m.name
from members m, cases c
where m.family_id = c.family_id
and c.id = :case_id
and m.member_type = 'guardian'
and m.deleted = false
and c.deleted = false

-- name: get-members
-- Returns all member names and id's
select * from members where deleted = false

-- name: update-sortname!
-- Updates the sortname
update members set sortname = :sortname where id = :id

-- name: get-members-for-family
-- Returns all members of a given type for a given family
select * from members where family_id = :family_id and member_type = :type and deleted = false order by dob

-- name: get-all-members-for-family
-- Returns all members for a given family ordered by guardians, children, and others
select * from members where family_id = :family_id and deleted = false order by array_search(member_type,array['guardian','child','other']), sortname, name

-- name: delete-member!
-- Deletes the member with a given ID by setting that records deleted flag to true
update members set deleted = true where id = :id

-- name: update-member!
-- Updates the member with a given ID based on the given data
update members
    set (name, sortname, dob, member_type, school) = (:name, :sortname, :dob, :member_type, :school)
where id = :id

-- name: new-member-for-family<!
-- Creates and returns a new member record for the given family id
insert into members (name, sortname, dob, member_type, school, family_id)
values (:name, :sortname, :dob, :member_type, :school, :family_id)

