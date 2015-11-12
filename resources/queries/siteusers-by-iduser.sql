-- siteusers by iduser
select s.iduser, s.idsiteuser, s.tuid, s.tpid, s.eapid, s.siteid, s.calid, s.locale, s.createdate from caluser."siteusers" s where s.iduser = :iduser
