-- retrieve expuser by siteid tuid
select e.iduser, e.expuserid, e.email, e.createdate from caluser."expusers" e, caluser."siteusers" s where e.iduser = s.iduser and s.siteid = :siteid and s.tuid = :tuid
