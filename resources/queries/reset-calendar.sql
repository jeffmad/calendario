--  reset-calendar
update caluser."siteusers" set calid = :uuid::uuid where siteid = :siteid and tuid = :tuid;
