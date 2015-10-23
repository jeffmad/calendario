-- user by siteid-tuid
select e.email, e.expuserid, s.tpid, s.eapid, s.siteid, s.calid from caluser."expusers" e, caluser."siteusers" s where s.iduser = e.iduser and s.tuid = :tuid and s.siteid = :siteid;
