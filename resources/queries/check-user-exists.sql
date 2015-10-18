-- check user exists by joining expusers and site user
select e.email, e.iduser, e.expuserid, s.idsiteuser, s.calid, s.tpid, s.eapid, s.tuid, s.siteid from caluser."expusers" e, caluser."siteusers" s where e.email = :email and s.calid = :uuid::uuid and e.iduser = s.iduser;
