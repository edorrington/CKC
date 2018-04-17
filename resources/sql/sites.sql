-- name: get-sites
-- Returns a list of all sites
select * from sites where is_active = true and id > 0

-- name: get-site-by-id
-- Returns an individual site
select * from sites where id = :id and is_active = true

-- name: get-site-admins-for-site
-- Returns a list of administrators for the given site id
select id from users where role = 'site-admin' and site_id = :id and is_active = true

-- name: get-site-admins
-- Returns a list of site ids and administrators
select s.id, u.id from sites s, users u where s.id = u.site_id and u.role = 'site-admin' and s.is_active = true

-- name: get-site-name
-- Returns the name of the site with id :id
select name from sites where id = :id and is_active = true
