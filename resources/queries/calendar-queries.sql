-- name: add-calendar<!
-- add a row to the calendar table
INSERT INTO cal."calendars" (idcalendar, icaltext, createdate) VALUES (:idcalendar, :icaltext, :createdate)
-- name: add-expuser<!
-- add a row to the expusers table
INSERT INTO caluser."expusers" (expuserid, email, createdate) VALUES (:expuserid, LOWER(:email), :createdate)
-- name: add-siteuser<!
-- add a row to the siteusers table
INSERT INTO caluser."siteusers" (iduser, calid, tpid, eapid, tuid, siteid, locale, createdate) VALUES (:iduser, :calid::uuid, :tpid, :eapid, :tuid, :siteid, :locale, :createdate)
-- name: associate-cal-to-user<!
-- associate a calendar to a user by adding a row to calendarsusers table
INSERT INTO cal."calendarsusers" (idcalendar, idsiteuser, createdate) VALUES (:idcalendar, :idsiteuser, :createdate)
-- name: find-active-users-with-expiring-calendars
-- find users with calendars that will soon expire
select s.siteid, s.tuid, s.idsiteuser from caluser.siteusers s, cal.calendaraccess ca where s.idsiteuser = ca.idsiteuser and ca.lastaccess > current_timestamp - interval '23 hours 45 minutes' and ca.lastaccess < current_timestamp - interval '23 hours 30 minutes'
-- name: calendar-accessed-recently
-- check if user has accessed calendar recently
select count(ca.idsiteuser) from cal.calendaraccess ca, caluser.siteusers s where s.tuid = :tuid and s.siteid = :siteid and ca.lastaccess > current_timestamp - interval '24 hours' and s.idsiteuser = ca.idsiteuser
-- name: check-user-exists
-- check user exists by joining expusers and site user
select e.email, e.iduser, e.expuserid, s.idsiteuser, s.calid, s.tpid, s.eapid, s.tuid, s.siteid, s.locale from caluser."expusers" e, caluser."siteusers" s where e.email = LOWER(:email) and s.calid = :uuid::uuid and e.iduser = s.iduser;
-- name: expuser-by-siteid-tuid
-- retrieve expuser by siteid tuid
select e.iduser, e.expuserid, e.email, e.createdate from caluser."expusers" e, caluser."siteusers" s where e.iduser = s.iduser and s.siteid = :siteid and s.tuid = :tuid
-- name: latest-calendar-created-for-user
-- pass in email and uuid, get icaltext if it exists, use greatest-n-per-group to get most recent calendar for user
with latest_cals as (
select a.idcalendar, a.idsiteuser, a.createdate from cal."calendarsusers" a inner join ( select idsiteuser, MAX(createdate) as d from cal."calendarsusers" group by idsiteuser ) b on a.idsiteuser = b.idsiteuser and a.createdate = b.d
)
select c.createdate
from caluser."siteusers" s,
latest_cals lc,
cal."calendars" c
where s.idsiteuser = :idsiteuser
and s.idsiteuser = lc.idsiteuser
and c.idcalendar = lc.idcalendar
and c.createdate >= date_trunc('month', current_date - interval '2 days')
and c.createdate < date_trunc('month', current_date)+'1month'
and lc.createdate >= date_trunc('month', current_date - interval '2 days')
and lc.createdate < date_trunc('month', current_date)+'1month'
ORDER BY c.createdate DESC LIMIT 1
-- name: latest-calendar-text-for-user
-- pass in email and uuid, get icaltext if it exists, use greatest-n-per-group to get most recent calendar for user
with latest_cals as (
select a.idcalendar, a.idsiteuser, a.createdate from cal."calendarsusers" a inner join ( select idsiteuser, MAX(createdate) as d from cal."calendarsusers" group by idsiteuser ) b on a.idsiteuser = b.idsiteuser and a.createdate = b.d
)
select c.icaltext, c.createdate, s.idsiteuser, s.siteid, s.tuid
from caluser."expusers" e,
caluser."siteusers" s,
latest_cals lc,
cal."calendars" c
where e.email = LOWER(:email)
and s.calid = :calid::uuid
and s.idsiteuser = lc.idsiteuser
and c.idcalendar = lc.idcalendar
and c.createdate >= date_trunc('month', current_date)
and c.createdate < date_trunc('month', current_date)+'1month'
and lc.createdate >= date_trunc('month', current_date)
and lc.createdate < date_trunc('month', current_date)+'1month'
ORDER BY c.createdate DESC LIMIT 1
-- name: next-calendar-id
-- get next seq for calendar. because pg does insert into master table no values returned.
select nextval('calendars_idcalendar_seq'::regclass)
-- name: record-calendar-access<!
-- insert a row to record the fact that the calendar was accessed
insert into calendaraccess (idsiteuser, lastaccess) values (:idsiteuser, :now)
-- name: reset-calendar!
-- assign the user a new uuid to access the calendar
update caluser."siteusers" set calid = :uuid::uuid where siteid = :siteid and tuid = :tuid
-- name: siteuser-by-siteid-tuid
-- return siteuser by siteid-tuid
select s.iduser, s.idsiteuser, s.tuid, s.tpid, s.eapid, s.siteid, s.calid, s.locale from caluser."siteusers" s where s.tuid = :tuid and s.siteid = :siteid
-- name: siteusers-by-iduser
-- return all siteusers by iduser
select s.iduser, s.idsiteuser, s.tuid, s.tpid, s.eapid, s.siteid, s.calid, s.locale, s.createdate from caluser."siteusers" s where s.iduser = :iduser
