-- add siteuser
INSERT INTO caluser."siteusers" (iduser, calid, tpid, eapid, tuid, siteid, locale, createdate) VALUES (:iduser, :calid::uuid, :tpid, :eapid, :tuid, :siteid, :locale, :createdate)
