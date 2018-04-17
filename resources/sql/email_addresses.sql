-- name: delete-email-address!
-- Deletes the email address with a given ID
delete from email_addresses where id = :id

-- name: update-email-address!
-- Updates the email address with a given ID based on the given data
update email_addresses
    set (email_address, email_address_type) = (:name, :type)
where id = :id

-- name: new-email-for-family<!
-- Creates and returns a new email address record for the given family id
insert into email_addresses (email_address, email_address_type, family_id)
values (:name, :type, :family_id)

