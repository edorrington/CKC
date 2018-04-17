-- name: make-new-outreach-event<!
-- Creates a new outreach event record
insert into outreach_events (site_id, start_time, end_time, title, location, description, type, enrollment_assistance, attendance)
values (:site_id, :start_time, :end_time, :title, :location, :description, :type, :enrollment_assistance, :attendance)

-- name: get-outreach-events
-- Returns a list of all outreach events for a given site within a given range ordered by start_time
select oe.*, 100 as position from outreach_events oe
where oe.site_id = :site_id
and oe.start_time >= :start
and oe.end_time < :end
order by oe.start_time desc

-- name: get-all-outreach-events
-- Returns a list of all outreach events within a given range ordered by start_time
select oe.*, 100 as position from outreach_events oe
where oe.start_time >= :start
and oe.end_time < :end
order by oe.start_time desc

-- name: get-outreach-event
-- Returns the given outreach event
select oe.*, 100 as position from outreach_events oe
where oe.id = :id
and oe.site_id = :site_id

-- name: update-event-times!
-- Updates the given event with the given times
update outreach_events
    set start_time = :start_time,
        end_time = :end_time
    where id = :id
    and site_id = :site_id

-- name: update-event!
-- Updates the given event with the given data
update outreach_events
    set title = :title,
        location = :location,
        description = :description,
        enrollment_assistance = :enrollment_assistance,
        attendance = :attendance,
        start_time = :start_time,
        end_time = :end_time
    where id = :id
    and site_id = :site_id

-- name: make-new-appointment<!
-- Creates a new appointment record
insert into appointments (case_id, site_id, start_time, end_time, title, location, description, type, r_email, r_mail, r_sms)
values (:case_id, :site_id, :start_time, :end_time, :title, :location, :description, :type, :r_email, :r_mail, :r_sms)

-- name: get-appointments
-- Returns a list of all appointments for a given site within a given range ordered by start_time
select a.*, at.position from appointments a, appointment_types at
where a.site_id = :site_id
and a.type = at.name
and a.type in (:show)
and a.start_time >= :start
and a.end_time < :end
order by a.start_time desc

-- name: get-all-appointments
-- Returns a list of all appointments within a given range ordered by start_time
select a.*, at.position from appointments a, appointment_types at
where a.type = at.name
and a.type in (:show)
and a.start_time >= :start
and a.end_time < :end
order by a.start_time desc

-- name: get-appointment
-- Returns the given appointment
select a.*, at.position from appointments a, appointment_types at
where a.id = :id
and a.site_id = :site_id
and a.type = at.name

-- name: update-appointment-times!
-- Updates the given appointment with the given times
update appointments
    set start_time = :start_time,
        end_time = :end_time
    where id = :id
    and site_id = :site_id

-- name: update-appointment!
-- Updates the given appointment with the given data
update appointments
    set type = :type,
        title = :title,
        location = :location,
        description = :description,
        start_time = :start_time,
        end_time = :end_time,
        r_email = :r_email,
        r_mail = :r_mail,
        r_sms = :r_sms
    where id = :id
    and site_id = :site_id

-- name: delete-appointment!
-- Deletes the appointment with the given id within the given site_id
delete from appointments where id = :id and site_id = :site_id

-- name: delete-event!
-- Deletes the event with the given id within the given site_id
delete from outreach_events where id = :id and site_id = :site_id

-- name: outstanding-emails
-- Returns appointments wanting a reminder email that haven't received one, for a given site
select a.*
from appointments a
where a.site_id = :site_id
and a.r_email = true
and a.s_email = false
and a.start_time < current_timestamp
order by a.start_time

-- name: outstanding-mails
-- Returns appointments wanting a reminder mail that haven't received one, for a given site
select a.*
from appointments a
where a.site_id = :site_id
and a.r_mail = true
and a.s_mail = false
and a.start_time < current_timestamp
order by a.start_time

-- name: outstanding-smses
-- Returns appointments wanting a reminder sms that haven't received one, for a given site
select a.*
from appointments a
where a.site_id = :site_id
and a.r_sms = true
and a.s_sms = false
and a.start_time < current_timestamp
order by a.start_time

-- name: sent-email!
-- Updates a given appointment to show that its email reminder has been sent
update appointments
set s_email = true
where id = :id

-- name: sent-mail!
-- Updates a given appointment to show that its mail reminder has been sent
update appointments
set s_email = true
where id = :id

-- name: sent-sms!
-- Updates a given appointment to show that its sms reminder has been sent
update appointments
set s_email = true
where id = :id

-- name: get-next-appointment-id
-- Returns the id of the next appointment to occur for a given site
select a.id
from appointments a
where a.start_time > current_timestamp
and a.site_id = :site_id
order by a.start_time
limit 1
