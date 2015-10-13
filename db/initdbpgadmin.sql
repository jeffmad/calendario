/** This is the initial DB script that needs to be executed after a fresh POSTGRES SQL installation
This script can be executed either from plsql command prompt or the pgadmin sql editor window.
POSTGRES SQL initial installation creates a default database called postgres.
You always connect to a DATABASE when you connect to POSTGRES SQL server instance. A fresh installation
always connect to the default postgres database.


Run this from PGADMIN - open the sql editor window and paste the contents below and execute it.

**/




CREATE ROLE webdbas;
ALTER ROLE webdbas SET search_path=cal,caluser,tz,public;


CREATE ROLE webusers;
ALTER ROLE webusers SET search_path=cal,caluser,tz,public;



ALTER USER root VALID UNTIL 'infinity';
GRANT webdbas TO root;



CREATE ROLE caldba LOGIN
NOSUPERUSER INHERIT CREATEDB CREATEROLE NOREPLICATION;

ALTER ROLE caldba
SET search_path = cal, caluser, tz, public;
GRANT webdbas TO caldba;
ALTER USER caldba WITH PASSWORD '333BlackTower';
ALTER USER caldba VALID UNTIL 'infinity';


create USER calweb password '333BlackTower';
ALTER ROLE calweb INHERIT;
GRANT webusers to calweb;
GRANT webusers to caldba;


create database caldb with owner = webdbas;
alter database caldb set search_path=cal, caluser, tz, public;

GRANT CONNECT ON DATABASE caldb TO webusers;


--------------------------------------------------------
-- hstore
--------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
