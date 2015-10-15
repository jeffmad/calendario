-- user-by-expuserid
select iduser, calid, expuserid, tpid, eapid, tuid, email, createdate from calusers where expuserid = :expuserid;
