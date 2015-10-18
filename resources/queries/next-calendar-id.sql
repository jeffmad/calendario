-- get next seq for calendar. because pg does insert into master table no values returned.
select nextval('calendars_idcalendar_seq'::regclass);
