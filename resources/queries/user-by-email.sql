-- user-by-email
select e.iduser, e.email, s.idsiteuser, s.calid, s.tpid, s.eapid, s.tuid, s.siteid from caluser."expusers" e, caluser."siteusers" s where e.email  = :email and e.iduser = s.iduser;
