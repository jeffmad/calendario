--  reset-calendar
update caluser."siteusers" set calid = :uuid where siteid = :siteid and tuid = :tuid;
