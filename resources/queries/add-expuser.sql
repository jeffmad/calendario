-- add expuser
INSERT INTO caluser."expusers" (expuserid, email, createdate) VALUES (:expuserid, LOWER(:email), :createdate)
