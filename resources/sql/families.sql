-- name: derf
-- Returns a derf-ful
select id, language, notes, array(select name from members where members.family_id = families.id and members.member_type = 'guardian') as guardians from families

-- name: get-family
-- Returns the family record for a given id
select * from families where id = :id and deleted = false

-- name: get-family-for-site
-- Returns the family record for a given id
select f.*, e.display_name as ethnicity_name, l.display_name as language_name
from families f, ethnicities e, languages_spoken l
where f.id = :id
and f.site_id = :site_id
and f.deleted = false
and f.ethnicity = e.name
and f.language = l.name

-- name: get-family-for-case
-- Returns the family record who has a given case id
select f.* from families f, cases c where f.id = c.family_id and c.id = :case_id and f.site_id = :site_id and f.deleted = false

-- name: search-family-for-site
-- Returns a list of family data that matches a given search for a given site
select f.* from families f, members m where f.site_id = :site_id and  m.family_id = f.id
and m.name like :query and f.deleted = false

-- name: get-families
-- Returns all families for a given site
select families.*, cases.status, cases.id as case_id, t.telephone_number, max(cc.contacted_at) as last_contact
from families
join cases on (families.id = cases.family_id and cases.id = (select max(cases.id) from cases where cases.family_id = families.id))
left join telephone_numbers t on (families.id = t.family_id and t.telephone_type = 'Home')
left join case_contacts cc on cases.id = cc.case_id
where families.site_id = :site_id
and families.deleted = false
group by families.id, cases.status, cases.id, t.telephone_number
order by families.id

-- name: get-all-families
-- Returns all families
select families.*, cases.status, cases.id as case_id, t.telephone_number, max(cc.contacted_at) as last_contact, s.name as site_name
from families
join cases on (families.id = cases.family_id and cases.id = (select max(cases.id) from cases where cases.family_id = families.id))
left join telephone_numbers t on (families.id = t.family_id and t.telephone_type = 'Home')
left join case_contacts cc on cases.id = cc.case_id
inner join sites s on families.site_id = s.id
where families.deleted = false
group by families.id, cases.status, cases.id, t.telephone_number, s.name
order by families.id

--name: matching-families
-- Returns a list of (family_id, name+phone) tuples for families whose member names/phones match the given query
select m.family_id as fid, c.id as cid, concat(m.name,' | ',t.telephone_number) as name
from members m, telephone_numbers t, families f, cases c
where m.family_id = f.id
and t.family_id = f.id
and f.site_id = :site_id
and c.family_id = f.id
and c.id = (select max(id) from cases c where c.family_id = f.id and c.deleted = false)
and (m.name ilike :query or t.telephone_number ilike :query)
and f.deleted = false
order by m.sortname

-- name: get-emails-for-family
-- Returns all email addresses for a given family
select * from email_addresses where family_id = :family_id

-- name: get-telephones-for-family
-- Returns all telephone numbers for a given family
select tn.*
from telephone_numbers tn, telephone_types tt
where tn.telephone_type = tt.name
and tn.family_id = :family_id
order by tt.position asc

-- name: new-family<!
-- Returns a new family for a given site
insert into families (site_id) values (:site_id)

-- name: delete-family!
-- Deletes the family with a given ID by setting that records deleted flag to true
update families set deleted = true where id = :id

-- name: update-family!
-- Updates the family with the given id
update families
    set (street1, city, zipcode, language, ethnicity, monthly_income, consent_signed, notes) = (:street1, :city, :zipcode, :language, :ethnicity, :monthly_income, :consent_signed, :notes)
where id = :id

-- name: create-family<!
-- Creates a new family record
insert into families (street1, city, zipcode, language, ethnicity, monthly_income, consent_signed, notes, site_id)
values (:street1, :city, :zipcode, :language, :ethnicity, :monthly_income, :consent_signed, :notes, :site_id)

-- name: get-all-family-data
-- Returns all families
select  families.*,
        cases.status,
        cases.id as case_id,
        t.telephone_number,
        max(cc.contacted_at) as last_contact,
        s.name as site_name,
        array(select name from members where family_id = families.id and member_type = 'guardian') as guardians,
        array(select name from members where family_id = families.id and member_type = 'child') as kids,
        (select sortname from members where family_id = families.id and member_type = 'guardian' limit 1) as sortname,
        (select display_name from ethnicities where ethnicities.name = families.ethnicity) as ethnicity_name,
        (select display_name from languages_spoken where languages_spoken.name = families.language) as language_name
from families
join cases on (families.id = cases.family_id and cases.id = (select max(cases.id) from cases where cases.family_id = families.id))
left join telephone_numbers t on (families.id = t.family_id and t.telephone_type = 'Home')
left join case_contacts cc on cases.id = cc.case_id
inner join sites s on families.site_id = s.id
where families.deleted = false
group by families.id, cases.status, cases.id, t.telephone_number, s.name
order by families.id


-- name: get-family-data
-- Returns all families for a given site
select  families.*,
        cases.status,
        cases.id as case_id,
        t.telephone_number,
        max(cc.contacted_at) as last_contact,
        s.name as site_name,
        array(select name from members where family_id = families.id and member_type = 'guardian') as guardians,
        array(select name from members where family_id = families.id and member_type = 'child') as kids,
        (select sortname from members where family_id = families.id and member_type = 'guardian' limit 1) as sortname,
        (select display_name from ethnicities where ethnicities.name = families.ethnicity) as ethnicity_name,
        (select display_name from languages_spoken where languages_spoken.name = families.language) as language_name
from families
join cases on (families.id = cases.family_id and cases.id = (select max(cases.id) from cases where cases.family_id = families.id))
left join telephone_numbers t on (families.id = t.family_id and t.telephone_type = 'Home')
left join case_contacts cc on cases.id = cc.case_id
inner join sites s on families.site_id = s.id
where families.site_id = :site_id
and families.deleted = false
group by families.id, cases.status, cases.id, t.telephone_number, s.name
order by families.id
