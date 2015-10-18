-- pass in email and uuid, get icaltext if it exists, use greatest-n-per-group to get most recent calendar for user
with latest_cals as (
select a.idcalendar, a.idsiteuser, a.createdate from cal."calendarsusers" a inner join ( select idsiteuser, MAX(createdate) as d from cal."calendarsusers" group by idsiteuser ) b on a.idsiteuser = b.idsiteuser and a.createdate = b.d
)
select c.icaltext, c.createdate, s.idsiteuser, s.tpid, s.tuid
from caluser."expusers" e,
caluser."siteusers" s,
latest_cals lc,
cal."calendars" c
where e.email = :email
and s.calid = :calid::uuid
and s.idsiteuser = lc.idsiteuser
and c.idcalendar = lc.idcalendar
and c.createdate >= date_trunc('month', current_date)
and c.createdate < date_trunc('month', current_date)+'1month'
and lc.createdate >= date_trunc('month', current_date)
and lc.createdate < date_trunc('month', current_date)+'1month';
