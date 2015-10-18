-- pass in email and uuid, get icaltext if it exists
with latest_cals as (
select a.idcalendar, a.idsiteuser, a.createdate from cal."calendarsusers" a inner join ( select idsiteuser, MAX(createdate) as d from cal."calendarsusers" group by idsiteuser ) b on a.idsiteuser = b.idsiteuser and a.createdate = b.d
)
select c.icaltext, c.createdate
from caluser."expusers" e,
caluser."siteusers" s,
latest_cals lc,
cal."calendars" c
where e.email = 'jeffmad@gmail.com'
and s.calid = '6eb7f25d-4bbf-4753-aae2-72635fcd959b'
and s.idsiteuser = lc.idsiteuser
and c.idcalendar = lc.idcalendar
and c.createdate >= date_trunc('month', current_date)
and c.createdate < date_trunc('month', current_date)+'1month'
and lc.createdate >= date_trunc('month', current_date)
and lc.createdate < date_trunc('month', current_date)+'1month';
