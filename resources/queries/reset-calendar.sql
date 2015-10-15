--  reset-calendar
update caluser."calusers" set calid = :uuid where expuserid = :expuserid;
