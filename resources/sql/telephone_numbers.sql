-- name: delete-telephone-number!
-- Deletes the telephone-number with a given ID
delete from telephone_numbers where id = :id

-- name: update-telephone-number!
-- Updates the telephone number with a given ID based on the given data
update telephone_numbers
    set (telephone_number, telephone_type) = (:name, :type)
where id = :id

-- name: new-phone-for-family<!
-- Creates and returns a new email address record for the given family id
insert into telephone_numbers (telephone_number, telephone_type, family_id)
values (:name, :type, :family_id)

