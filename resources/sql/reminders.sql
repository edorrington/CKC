-- name: has-template-for-language
-- Returns whether a template exists for a given site in a given language of a given type
select count(*)=1
from reminder_templates
where site_id = :site_id
and type = :type
and language = :language

-- name: has-preferred-template
-- Returns whether a template for a given type in a given site for a given family exists in their language
select count(*)=1
from reminder_templates rt, families f
where f.site_id = rt.site_id
and rt.language = f.language
and rt.type = :type
and f.id = :family_id

-- name: get-template
-- Returns the appropriate template for a given type for a given family (their preferred language if available, otherwise english if available)
select rt.content, rt.language from reminder_templates rt, families f
where rt.type = :type
and f.site_id = rt.site_id
and f.id = :family_id
and rt.language = f.language
or (rt.language = 'english' and rt.type = :type and f.site_id = rt.site_id and f.id = :family_id and not exists
    (select * from reminder_templates rt2, families f2
     where rt2.type = :type
     and f2.site_id = rt2.site_id
     and rt2.language = f2.language
     and f.id = f2.id))

-- name: get-templates-for-type
-- Returns all templates for a given site and a given type
select rt.*
from reminder_templates rt, languages_spoken l
where site_id = :site_id
and type = :type
and rt.language = l.name
order by l.position

-- name: new-reminder-template<!
-- Creates a new reminder template of a given type for a given site
insert into reminder_templates (site_id, type, language, content)
values (:site_id, :type, :language, :content)

-- name: update-reminder-template!
-- Updates the given reminder_template
update reminder_templates
set content = :content
where id = :id

-- name: delete-reminder-template!
-- Deletes the given reminder template
delete from reminder_templates where id = :id

-- name: get-reminder-schedule
-- Returns the reminder schedule for a given type at a given site_id
select days, hours from reminder_schedules where site_id = :site_id and type = :type

-- name: update-reminder-schedule!
-- Updates the reminder type with a given name, for a given site_id, to have the given schedule
update reminder_schedules
set (days, hours) = (:days, :hours)
where site_id = :site_id
and type = :type

-- name: get-full-appointment
-- Returns all the data needed to send reminders for a given appointment
select a.start_time, a.end_time, a.type, c.family_id, f.street1, f.city, f.zipcode
from appointments a, families f, cases c
where a.id = :id
and a.case_id = c.id
and c.family_id = f.id
