-- given email and siteid, return guid for that site for the expuser
select s.calid from caluser."expusers" e, caluser."siteusers" s where e.email = :email and s.siteid = :siteid;
