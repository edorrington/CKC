-- name: get-referrals
-- Returns all referrals for a given site
select * from referrals where site_id = :site_id

-- name: get-ytd-referrals
-- Returns all referrals for a given site in the current year
select identified_need, service_provided, count(*) from referrals where site_id = :site_id
and extract(year from referral_date) = extract(year from current_timestamp)
group by identified_need, service_provided;

-- name: get-mtd-referrals
-- Returns all referrals for a given site in the current month
select identified_need, service_provided, count(*) from referrals where site_id = :site_id
and extract(year from referral_date) = extract(year from current_timestamp)
and extract(month from referral_date) = extract(month from current_timestamp)
group by identified_need, service_provided;

-- name: add-referral<!
-- Inserts a new referral record
insert into referrals (identified_need, service_provided, referral_date, site_id)
values (:identified_need, :service_provided, :referral_date, :site_id)
