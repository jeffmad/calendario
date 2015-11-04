-- check if user has accessed calendar recently
select count(ca.idsiteuser) from cal.calendaraccess ca, caluser.siteusers s where s.tuid = :tuid and s.siteid = :siteid and ca.lastaccess > current_timestamp - interval '24 hours' and s.idsiteuser = ca.idsiteuser
