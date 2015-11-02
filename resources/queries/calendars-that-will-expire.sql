-- find users with calendars that will soon expire
select s.siteid, s.tuid, s.idsiteuser from caluser.siteusers s, cal.calendaraccess ca where s.idsiteuser = ca.idsiteuser and ca.lastaccess > current_timestamp - interval '23 hours 45 minutes' and ca.lastaccess < current_timestamp - interval '23 hours 30 minutes'
