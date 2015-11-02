-- insert a row to record the fact that the calendar was accessed
insert into calendaraccess (idsiteuser, lastaccess) values (:idsiteuser, :now);
