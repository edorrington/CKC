-- name: create-user!
-- Inserts a new user record
insert into users (id, pass, site_id, role)
    values (:id, :pass, :site_id, :role)

-- name: update-user!
-- Updates the given users data
update users
    set first_name = :first_name,
    last_name = :last_name,
    email = :email
where id = :id

-- name: update-user-password!
-- Updates the password of the user with the given id
update users
    set pass = :pass
    where id = :id

-- name: user-with-events
-- Returns the user record and all associated events for :id
select * from users left outer join events on events.users_id = users.id where users.id = :id

-- name: active-users
-- Returns a list of all active users
select * from users where is_active = 't'

-- name: get-users
-- Returns a list of all users
select * from users

-- name: get-user-by-id
-- Returns the user with id :id
select * from users where id = :id

-- name: get-users-for-site
-- Returns all users associated with the given site id
select * from users where site_id = :id

-- name: get-users-for-site-name
-- Returns all users associated with the given site name
select * from users where site_id = (select id from sites where name = :name)

-- name: user-exists?
-- Returns true if user with id :id exists
select count(*) > 0 as exists from users where id = :id
