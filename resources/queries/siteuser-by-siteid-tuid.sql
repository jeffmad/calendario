-- siteuser by siteid-tuid
select s.iduser, s.idsiteuser, s.tuid, s.tpid, s.eapid, s.siteid, s.calid, s.locale from caluser."siteusers" s where s.tuid = :tuid and s.siteid = :siteid
