-- name: get-schedules
-- Returns schedule items for a given site_id for all assister types within a given date range
select s.id as id,
       s.type,
       si.id as item_id,
       si.start_time,
       si.end_time,
       si.slots,
       s.repeats,
       s.repeat_begin,
       s.repeat_end,
       at.position
from schedule_items si, appointment_types at, schedules s
where si.schedule_id = s.id
and s.site_id = :site_id
and s.type = at.name
and s.type in (:show)
and (:start, :end) overlaps (si.start_time, si.end_time)

-- name: make-new-schedule<!
-- Creates a new schedule record
insert into schedules (site_id, type, repeats, repeat_begin, repeat_end)
values (:site_id, :type, :repeats, :repeat_begin, :repeat_end)

-- name: make-new-schedule-item<!
-- Creates a new schedule item
insert into schedule_items (schedule_id, start_time, end_time, slots)
values (:schedule_id, :start_time, :end_time, :slots)

-- name: update-schedule-times!
-- Updates the given schedule with the given times
update schedules
    set start_time = :start_time,
        end_time = :end_time
    where id = :id
    and site_id = :site_id

-- name: update-schedule!
-- Updates the given schedule with the given data
update schedules
    set start_time = :start_time,
        end_time = :end_time,
        repeats = :repeats,
        repeat_begin = :repeat_begin,
        repeat_end = :repeat_end
    where id = :id
    and site_id = :site_id

-- name: delete-schedule!
-- Deletes the schedule with the given id within the given site_id
delete from schedules where id = :id and site_id = :site_id

-- name: split-schedule<!
-- Splits the schedule into two records based on the passed date
insert into schedules (site_id, start_time, end_time, type, slots, repeats, repeat_begin, repeat_end)
values (select site_id, start_time, end_time, type, slots, repeats, :date + interval '1 day', repeat_end
from schedules where id = :id and site_id = :site_id)

-- name: delete-future-schedule!
delete from schedule_items
where schedule_id = :schedule_id
and
