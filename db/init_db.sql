/** This is the initial DB script that needs to be executed after a fresh POSTGRES SQL installation
 This script can be executed either from plsql command prompt or the pgadmin sql editor window.
 POSTGRES SQL initial installation creates a default database called postgres.
 You always connect to a DATABASE when you connect to POSTGRES SQL server instance. A fresh installation
 always connect to the default postgres database.

 1. To run this script from the plsql command prompt issue this command
 at the default database prompt -   \i  <PATH to the sql file>/init_db.sql;
 Example:
 postgres=# \i  /Users/lraju/src/calendario/db/init_db.sql;

 2. To run this from PGADMIN - open the sql editor window and paste the contents below and execute it.

 **/


DO $$
BEGIN

IF NOT EXISTS ((select 1 from pg_roles where rolname = 'webdbas'))
    THEN
  CREATE ROLE webdbas;
 ALTER ROLE webdbas SET search_path=cal,caluser,tz,public;
END IF;

IF NOT EXISTS ((select 1 from pg_roles where rolname = 'webusers'))
    THEN
  CREATE ROLE webusers;
  ALTER ROLE webusers SET search_path=cal,caluser,tz,public;
END IF;

IF EXISTS ((select 1 from pg_user where usename = 'root'))
THEN
	ALTER USER root VALID UNTIL 'infinity';
	GRANT webdbas TO root;
END IF;

IF NOT EXISTS ((select 1 from pg_roles where rolname = 'caldba'))
    THEN
  CREATE ROLE caldba LOGIN
  NOSUPERUSER INHERIT CREATEDB CREATEROLE NOREPLICATION;

  ALTER ROLE caldba
  SET search_path = cal, caluser, tz, public;
  GRANT webdbas TO caldba;
  ALTER USER caldba WITH PASSWORD '333BlackTower';
  ALTER USER caldba VALID UNTIL 'infinity';
END IF;

IF NOT EXISTS ((select 1 from pg_roles where rolname = 'calweb'))
    THEN
  create USER calweb password '333BlackTower';
  ALTER ROLE calweb INHERIT;
  GRANT webusers to calweb;
  GRANT webusers to caldba;

END IF;

END$$;

create database caldb with owner = webdbas;
alter database caldb set search_path=cal, caluser, tz,public;

GRANT CONNECT ON DATABASE caldb TO webusers;

\c caldb
--------------------------------------------------------
-- hstore
--------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
