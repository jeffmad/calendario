-- retrieve expuser by email
select iduser, expuserid, email, createdate from caluser."expusers" where email = :email
