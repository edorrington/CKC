-- name: schools-for-site
-- Returns a list of all schools and their type for a given site
select *
from schools
where site_id = :site_id
order by id

-- name: school-types
-- Returns a list of school types
select * from school_types order by id
