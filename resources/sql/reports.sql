-- name: get-family-members
-- Returns name & DOB for family members that have been added between :from and :to
select f.id, m.sortname, m.name, m.dob
from families f, members m
where m.family_id = f.id
and m.created_at >= :from
and m.created_at < :to
and m.deleted = false
and f.deleted = false



-- name: outreach-calls-made
-- Returns the total number of outreach calls made during the given time period, for the given site
select count(f.id)
from families f, cases c, case_contacts cc, contact_types ct
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contact_type_id = ct.id
and ct.contact_type_category_id in (1,2,3)    -- Outreach calls
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: incoming-calls-fielded
-- Returns the total number of incoming calls handled during the given time period, for the given site
select count(f.id)
from families f, cases c, case_contacts cc, contact_types ct
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contact_type_id = ct.id
and ct.contact_type_category_id in (4,5)    -- Incoming calls
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: scheduled-appointments
-- Returns the number of families that scheduled appts as a result of a call during the given period for a given site
select count(f.id)
from families f, cases c, case_contacts cc, contact_types ct
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contact_type_id = ct.id
and ct.contact_type_category_id in (1,4)    -- Calls
and ct.appointment = true
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: interested-families
-- Returns the number of families "interested" (i.e. non-closing statuses) but who didn't make appointments
select count(f.id)
from families f, cases c, case_contacts cc, contact_types ct, case_status_types cst
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contact_type_id = ct.id
and ct.contact_type_category_id in (1,4)    -- Calls
and ct.appointment = false
and ct.suggested_case_status = cst.name
and cst.is_open = true
and cc.contacted_at >= :from
and cc.contacted_at < :to

-- name: interested-families2
-- Returns the number of families "interested" (i.e. contact_types that aren't already insured/don't want, etc.) but who didn't make appointments
select count(f.id)
from families f, cases c, case_contacts cc, contact_types ct
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contact_type_id = ct.id
and ct.appointment = false
and ct.id not in (4,5,6,17,18,19)
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: total-families-served
-- Returns the count of distinct families with any type of contact during the given period for the given site
select count(distinct(f.id))
from families f, cases c, case_contacts cc
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: total-contacts
-- Returns the total number of contacts logged with families during the given period for the given site
select count(f.id)
from families f, cases c, case_contacts cc
where f.site_id = :site_id
and f.deleted = false
and c.family_id = f.id
and cc.case_id = c.id
and cc.contacted_at >= :from
and cc.contacted_at < :to


-- name: outreach-event-data
-- Returns Outreach Event data for the given site during the given period
select * from outreach_events
where site_id = :site_id
and start_time >= :from
and end_time < :to
