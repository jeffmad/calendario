-- retrieve siteuser by iduser and siteid
select idsiteuser, iduser, calid, tpid, eapid, tuid, siteid, createdate from caluser."siteusers" where iduser = :iduser and siteid = :siteid
