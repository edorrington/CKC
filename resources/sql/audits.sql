-- name: get-audit-records
-- Returns all audit records for a given site
select ar.*, s.name
from audit_records ar, sites s
where ar.site_id = :site_id
and s.id = ar.site_id
order by executed_at desc

-- name: get-all-audit-records
-- Returns all audit records
select ar.*, s.name
from audit_records ar, sites s
where ar.site_id = s.id
order by ar.site_id, ar.id desc

-- name: get-n-audit-records
-- Returns the last 'n' records for a given site
select ar.*, s.name
from audit_records ar, sites s
where ar.site_id = :site_id
and ar.site_id = s.id
order by executed_at desc
limit :limit

-- name: get-paged-audit-records
-- Returns a page of size :limit records based on ordering given
select ar.*, s.name
from audit_records ar, sites s
where ar.site_id = s.id
order by :order
limit :limit
offset :offset

-- name: audit-records-count
-- Returns the # of audit records total
select count(*) from audit_records
