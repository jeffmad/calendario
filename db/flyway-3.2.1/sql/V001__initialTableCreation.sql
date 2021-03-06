create schema IF NOT EXISTS cal AUTHORIZATION  webdbas;
REVOKE ALL PRIVILEGES ON schema cal FROM GROUP PUBLIC;

GRANT ALL ON SCHEMA cal TO webdbas;
grant usage on schema cal to webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA cal grant SELECT, INSERT, UPDATE, DELETE on tables to webusers;
ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA cal GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA cal GRANT EXECUTE ON FUNCTIONS TO webusers;


create schema IF NOT EXISTS caluser AUTHORIZATION  webdbas;
REVOKE ALL PRIVILEGES ON schema caluser FROM GROUP PUBLIC;

GRANT ALL ON SCHEMA caluser TO webdbas;
grant usage on schema caluser to webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA caluser grant SELECT, INSERT, UPDATE, DELETE on tables to webusers;
ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA caluser GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA caluser GRANT EXECUTE ON FUNCTIONS TO webusers;


create schema IF NOT EXISTS tz AUTHORIZATION  webdbas;
REVOKE ALL PRIVILEGES ON schema tz FROM GROUP PUBLIC;

GRANT ALL ON SCHEMA tz TO webdbas;
grant usage on schema tz to webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA tz grant SELECT, INSERT, UPDATE, DELETE on tables to webusers;
ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA tz GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO webusers;

ALTER DEFAULT PRIVILEGES FOR ROLE caldba IN SCHEMA tz GRANT EXECUTE ON FUNCTIONS TO webusers;


ALTER ROLE caldba  SET search_path = cal, caluser, tz, public;
ALTER ROLE calweb  SET search_path = cal, caluser, tz, public;

-- -----------------------------------------------------
-- Table caluser."expusers"
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS caluser."expusers" (
iduser SERIAL PRIMARY KEY,
expuserid INTEGER NULL,
email VARCHAR(512),
createdate TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE caluser."expusers"
OWNER TO caldba;

-- -----------------------------------------------------
-- Creating indexes
-- -----------------------------------------------------
CREATE UNIQUE INDEX expusers_email_idx ON caluser."expusers" ((LOWER(email)));

-- -----------------------------------------------------
-- Table caluser."siteusers"
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS caluser."siteusers" (
idsiteuser SERIAL PRIMARY KEY,
iduser INTEGER REFERENCES caluser."expusers" (iduser),
calid UUID NOT NULL,
tpid INTEGER NOT NULL,
eapid INTEGER NOT NULL,
tuid INTEGER NOT NULL,
siteid INTEGER NOT NULL,
locale VARCHAR(8),
createdate TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE caluser."siteusers"
OWNER TO caldba;

-- -----------------------------------------------------
-- Creating indexes
-- -----------------------------------------------------

CREATE UNIQUE INDEX siteusers_siteid_tuid_idx ON caluser."siteusers" (siteid, tuid);

CREATE INDEX siteusers_iduser_idx ON caluser."siteusers" (iduser);
CREATE INDEX siteusers_calid_idx ON caluser."siteusers" (calid);

-- -----------------------------------------------------
-- Table cal."calendars"
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS cal."calendars" (
  idcalendar SERIAL PRIMARY KEY,
  icaltext TEXT NULL,
  createdate TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
 );

ALTER TABLE cal."calendars"
OWNER TO caldba;

-- -----------------------------------------------------
-- Table cal."calendarsusers"
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS cal."calendarsusers" (
   idcalendar INTEGER REFERENCES cal.calendars (idcalendar),
   idsiteuser INTEGER REFERENCES caluser.siteusers (idsiteuser),
   createdate TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE cal."calendarsusers"
OWNER TO caldba;

CREATE TABLE cal.calendars_y2015m10 (
    CHECK ( createdate >= DATE '2015-10-01' AND createdate < DATE '2015-11-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2015m11 (
    CHECK ( createdate >= DATE '2015-11-01' AND createdate < DATE '2015-12-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2015m12 (
    CHECK ( createdate >= DATE '2015-12-01' AND createdate < DATE '2016-01-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m01 (
    CHECK ( createdate >= DATE '2016-01-01' AND createdate < DATE '2016-02-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m02 (
    CHECK ( createdate >= DATE '2016-02-01' AND createdate < DATE '2016-03-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m03 (
    CHECK ( createdate >= DATE '2016-03-01' AND createdate < DATE '2016-04-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m04 (
    CHECK ( createdate >= DATE '2016-04-01' AND createdate < DATE '2016-05-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m05 (
    CHECK ( createdate >= DATE '2016-05-01' AND createdate < DATE '2016-06-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m06 (
    CHECK ( createdate >= DATE '2016-06-01' AND createdate < DATE '2016-07-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m07 (
    CHECK ( createdate >= DATE '2016-07-01' AND createdate < DATE '2016-08-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m08 (
    CHECK ( createdate >= DATE '2016-08-01' AND createdate < DATE '2016-09-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m09 (
    CHECK ( createdate >= DATE '2016-09-01' AND createdate < DATE '2016-10-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m10 (
    CHECK ( createdate >= DATE '2016-10-01' AND createdate < DATE '2016-11-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m11 (
    CHECK ( createdate >= DATE '2016-11-01' AND createdate < DATE '2016-12-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2016m12 (
    CHECK ( createdate >= DATE '2016-12-01' AND createdate < DATE '2017-01-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m01 (
    CHECK ( createdate >= DATE '2017-01-01' AND createdate < DATE '2017-02-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m02 (
    CHECK ( createdate >= DATE '2017-02-01' AND createdate < DATE '2017-03-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m03 (
    CHECK ( createdate >= DATE '2017-03-01' AND createdate < DATE '2017-04-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m04 (
    CHECK ( createdate >= DATE '2017-04-01' AND createdate < DATE '2017-05-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m05 (
    CHECK ( createdate >= DATE '2017-05-01' AND createdate < DATE '2017-06-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m06 (
    CHECK ( createdate >= DATE '2017-06-01' AND createdate < DATE '2017-07-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m07 (
    CHECK ( createdate >= DATE '2017-07-01' AND createdate < DATE '2017-08-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m08 (
    CHECK ( createdate >= DATE '2017-08-01' AND createdate < DATE '2017-09-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m09 (
    CHECK ( createdate >= DATE '2017-09-01' AND createdate < DATE '2017-10-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m10 (
    CHECK ( createdate >= DATE '2017-10-01' AND createdate < DATE '2017-11-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m11 (
    CHECK ( createdate >= DATE '2017-11-01' AND createdate < DATE '2017-12-01' )
) INHERITS (cal.calendars);
CREATE TABLE cal.calendars_y2017m12 (
    CHECK ( createdate >= DATE '2017-12-01' AND createdate < DATE '2018-01-01' )
) INHERITS (cal.calendars);




CREATE INDEX calendars_y2015m10_createdate ON cal.calendars_y2015m10 (createdate);
CREATE INDEX calendars_y2015m11_createdate ON cal.calendars_y2015m11 (createdate);
CREATE INDEX calendars_y2015m12_createdate ON cal.calendars_y2015m12 (createdate);
CREATE INDEX calendars_y2016m01_createdate ON cal.calendars_y2016m01 (createdate);
CREATE INDEX calendars_y2016m02_createdate ON cal.calendars_y2016m02 (createdate);
CREATE INDEX calendars_y2016m03_createdate ON cal.calendars_y2016m03 (createdate);
CREATE INDEX calendars_y2016m04_createdate ON cal.calendars_y2016m04 (createdate);
CREATE INDEX calendars_y2016m05_createdate ON cal.calendars_y2016m05 (createdate);
CREATE INDEX calendars_y2016m06_createdate ON cal.calendars_y2016m06 (createdate);
CREATE INDEX calendars_y2016m07_createdate ON cal.calendars_y2016m07 (createdate);
CREATE INDEX calendars_y2016m08_createdate ON cal.calendars_y2016m08 (createdate);
CREATE INDEX calendars_y2016m09_createdate ON cal.calendars_y2016m09 (createdate);
CREATE INDEX calendars_y2016m10_createdate ON cal.calendars_y2016m10 (createdate);
CREATE INDEX calendars_y2016m11_createdate ON cal.calendars_y2016m11 (createdate);
CREATE INDEX calendars_y2016m12_createdate ON cal.calendars_y2016m12 (createdate);

CREATE INDEX calendars_y2017m01_createdate ON cal.calendars_y2017m01 (createdate);
CREATE INDEX calendars_y2017m02_createdate ON cal.calendars_y2017m02 (createdate);
CREATE INDEX calendars_y2017m03_createdate ON cal.calendars_y2017m03 (createdate);
CREATE INDEX calendars_y2017m04_createdate ON cal.calendars_y2017m04 (createdate);
CREATE INDEX calendars_y2017m05_createdate ON cal.calendars_y2017m05 (createdate);
CREATE INDEX calendars_y2017m06_createdate ON cal.calendars_y2017m06 (createdate);
CREATE INDEX calendars_y2017m07_createdate ON cal.calendars_y2017m07 (createdate);
CREATE INDEX calendars_y2017m08_createdate ON cal.calendars_y2017m08 (createdate);
CREATE INDEX calendars_y2017m09_createdate ON cal.calendars_y2017m09 (createdate);
CREATE INDEX calendars_y2017m10_createdate ON cal.calendars_y2017m10 (createdate);
CREATE INDEX calendars_y2017m11_createdate ON cal.calendars_y2017m11 (createdate);
CREATE INDEX calendars_y2017m12_createdate ON cal.calendars_y2017m12 (createdate);


CREATE OR REPLACE FUNCTION calendars_insert_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF ( NEW.createdate >= DATE '2015-10-01' AND
         NEW.createdate < DATE '2015-11-01' ) THEN
        INSERT INTO cal.calendars_y2015m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2015-11-01' AND
            NEW.createdate < DATE '2015-12-01' ) THEN
        INSERT INTO cal.calendars_y2015m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2015-12-01' AND
            NEW.createdate < DATE '2016-01-01' ) THEN
        INSERT INTO cal.calendars_y2015m12 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-01-01' AND
            NEW.createdate < DATE '2016-02-01' ) THEN
        INSERT INTO cal.calendars_y2016m01 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-02-01' AND
            NEW.createdate < DATE '2016-03-01' ) THEN
        INSERT INTO cal.calendars_y2016m02 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-03-01' AND
            NEW.createdate < DATE '2016-04-01' ) THEN
        INSERT INTO cal.calendars_y2016m03 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-04-01' AND
            NEW.createdate < DATE '2016-05-01' ) THEN
        INSERT INTO cal.calendars_y2016m04 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-05-01' AND
            NEW.createdate < DATE '2016-06-01' ) THEN
        INSERT INTO cal.calendars_y2016m05 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-06-01' AND
            NEW.createdate < DATE '2016-07-01' ) THEN
        INSERT INTO cal.calendars_y2016m06 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-07-01' AND
            NEW.createdate < DATE '2016-08-01' ) THEN
        INSERT INTO cal.calendars_y2016m07 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-08-01' AND
            NEW.createdate < DATE '2016-09-01' ) THEN
        INSERT INTO cal.calendars_y2016m08 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-09-01' AND
            NEW.createdate < DATE '2016-10-01' ) THEN
        INSERT INTO cal.calendars_y2016m09 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-10-01' AND
            NEW.createdate < DATE '2016-11-01' ) THEN
        INSERT INTO cal.calendars_y2016m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-11-01' AND
            NEW.createdate < DATE '2016-12-01' ) THEN
        INSERT INTO cal.calendars_y2016m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-12-01' AND
            NEW.createdate < DATE '2017-01-01' ) THEN
        INSERT INTO cal.calendars_y2016m12 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-01-01' AND
            NEW.createdate < DATE '2017-02-01' ) THEN
        INSERT INTO cal.calendars_y2017m01 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-02-01' AND
            NEW.createdate < DATE '2017-03-01' ) THEN
        INSERT INTO cal.calendars_y2017m02 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-03-01' AND
            NEW.createdate < DATE '2017-04-01' ) THEN
        INSERT INTO cal.calendars_y2017m03 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-04-01' AND
            NEW.createdate < DATE '2017-05-01' ) THEN
        INSERT INTO cal.calendars_y2017m04 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-05-01' AND
            NEW.createdate < DATE '2017-06-01' ) THEN
        INSERT INTO cal.calendars_y2017m05 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-06-01' AND
            NEW.createdate < DATE '2017-07-01' ) THEN
        INSERT INTO cal.calendars_y2017m06 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-07-01' AND
            NEW.createdate < DATE '2017-08-01' ) THEN
        INSERT INTO cal.calendars_y2017m07 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-08-01' AND
            NEW.createdate < DATE '2017-09-01' ) THEN
        INSERT INTO cal.calendars_y2017m08 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-09-01' AND
            NEW.createdate < DATE '2017-10-01' ) THEN
        INSERT INTO cal.calendars_y2017m09 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-10-01' AND
            NEW.createdate < DATE '2017-11-01' ) THEN
        INSERT INTO cal.calendars_y2017m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-11-01' AND
            NEW.createdate < DATE '2017-12-01' ) THEN
        INSERT INTO cal.calendars_y2017m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-12-01' AND
            NEW.createdate < DATE '2018-01-01' ) THEN
        INSERT INTO cal.calendars_y2017m12 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the calendars_insert_trigger() function!';
    END IF;
    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER insert_calendars_trigger
    BEFORE INSERT ON cal.calendars
    FOR EACH ROW EXECUTE PROCEDURE calendars_insert_trigger();
--
--
--
CREATE TABLE cal.calendarsusers_y2015m10 (
    CHECK ( createdate >= DATE '2015-10-01' AND createdate < DATE '2015-11-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2015m11 (
    CHECK ( createdate >= DATE '2015-11-01' AND createdate < DATE '2015-12-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2015m12 (
    CHECK ( createdate >= DATE '2015-12-01' AND createdate < DATE '2016-01-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m01 (
    CHECK ( createdate >= DATE '2016-01-01' AND createdate < DATE '2016-02-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m02 (
    CHECK ( createdate >= DATE '2016-02-01' AND createdate < DATE '2016-03-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m03 (
    CHECK ( createdate >= DATE '2016-03-01' AND createdate < DATE '2016-04-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m04 (
    CHECK ( createdate >= DATE '2016-04-01' AND createdate < DATE '2016-05-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m05 (
    CHECK ( createdate >= DATE '2016-05-01' AND createdate < DATE '2016-06-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m06 (
    CHECK ( createdate >= DATE '2016-06-01' AND createdate < DATE '2016-07-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m07 (
    CHECK ( createdate >= DATE '2016-07-01' AND createdate < DATE '2016-08-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m08 (
    CHECK ( createdate >= DATE '2016-08-01' AND createdate < DATE '2016-09-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m09 (
    CHECK ( createdate >= DATE '2016-09-01' AND createdate < DATE '2016-10-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m10 (
    CHECK ( createdate >= DATE '2016-10-01' AND createdate < DATE '2016-11-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m11 (
    CHECK ( createdate >= DATE '2016-11-01' AND createdate < DATE '2016-12-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2016m12 (
    CHECK ( createdate >= DATE '2016-12-01' AND createdate < DATE '2017-01-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m01 (
    CHECK ( createdate >= DATE '2017-01-01' AND createdate < DATE '2017-02-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m02 (
    CHECK ( createdate >= DATE '2017-02-01' AND createdate < DATE '2017-03-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m03 (
    CHECK ( createdate >= DATE '2017-03-01' AND createdate < DATE '2017-04-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m04 (
    CHECK ( createdate >= DATE '2017-04-01' AND createdate < DATE '2017-05-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m05 (
    CHECK ( createdate >= DATE '2017-05-01' AND createdate < DATE '2017-06-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m06 (
    CHECK ( createdate >= DATE '2017-06-01' AND createdate < DATE '2017-07-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m07 (
    CHECK ( createdate >= DATE '2017-07-01' AND createdate < DATE '2017-08-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m08 (
    CHECK ( createdate >= DATE '2017-08-01' AND createdate < DATE '2017-09-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m09 (
    CHECK ( createdate >= DATE '2017-09-01' AND createdate < DATE '2017-10-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m10 (
    CHECK ( createdate >= DATE '2017-10-01' AND createdate < DATE '2017-11-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m11 (
    CHECK ( createdate >= DATE '2017-11-01' AND createdate < DATE '2017-12-01' )
) INHERITS (cal.calendarsusers);
CREATE TABLE cal.calendarsusers_y2017m12 (
    CHECK ( createdate >= DATE '2017-12-01' AND createdate < DATE '2018-01-01' )
) INHERITS (cal.calendarsusers);


CREATE INDEX calendarsusers_y2015m10_createdate ON cal.calendarsusers_y2015m10 (createdate);
CREATE INDEX calendarsusers_y2015m11_createdate ON cal.calendarsusers_y2015m11 (createdate);
CREATE INDEX calendarsusers_y2015m12_createdate ON cal.calendarsusers_y2015m12 (createdate);
CREATE INDEX calendarsusers_y2016m01_createdate ON cal.calendarsusers_y2016m01 (createdate);
CREATE INDEX calendarsusers_y2016m02_createdate ON cal.calendarsusers_y2016m02 (createdate);
CREATE INDEX calendarsusers_y2016m03_createdate ON cal.calendarsusers_y2016m03 (createdate);
CREATE INDEX calendarsusers_y2016m04_createdate ON cal.calendarsusers_y2016m04 (createdate);
CREATE INDEX calendarsusers_y2016m05_createdate ON cal.calendarsusers_y2016m05 (createdate);
CREATE INDEX calendarsusers_y2016m06_createdate ON cal.calendarsusers_y2016m06 (createdate);
CREATE INDEX calendarsusers_y2016m07_createdate ON cal.calendarsusers_y2016m07 (createdate);
CREATE INDEX calendarsusers_y2016m08_createdate ON cal.calendarsusers_y2016m08 (createdate);
CREATE INDEX calendarsusers_y2016m09_createdate ON cal.calendarsusers_y2016m09 (createdate);
CREATE INDEX calendarsusers_y2016m10_createdate ON cal.calendarsusers_y2016m10 (createdate);
CREATE INDEX calendarsusers_y2016m11_createdate ON cal.calendarsusers_y2016m11 (createdate);
CREATE INDEX calendarsusers_y2016m12_createdate ON cal.calendarsusers_y2016m12 (createdate);

CREATE INDEX calendarsusers_y2017m01_createdate ON cal.calendarsusers_y2017m01 (createdate);
CREATE INDEX calendarsusers_y2017m02_createdate ON cal.calendarsusers_y2017m02 (createdate);
CREATE INDEX calendarsusers_y2017m03_createdate ON cal.calendarsusers_y2017m03 (createdate);
CREATE INDEX calendarsusers_y2017m04_createdate ON cal.calendarsusers_y2017m04 (createdate);
CREATE INDEX calendarsusers_y2017m05_createdate ON cal.calendarsusers_y2017m05 (createdate);
CREATE INDEX calendarsusers_y2017m06_createdate ON cal.calendarsusers_y2017m06 (createdate);
CREATE INDEX calendarsusers_y2017m07_createdate ON cal.calendarsusers_y2017m07 (createdate);
CREATE INDEX calendarsusers_y2017m08_createdate ON cal.calendarsusers_y2017m08 (createdate);
CREATE INDEX calendarsusers_y2017m09_createdate ON cal.calendarsusers_y2017m09 (createdate);
CREATE INDEX calendarsusers_y2017m10_createdate ON cal.calendarsusers_y2017m10 (createdate);
CREATE INDEX calendarsusers_y2017m11_createdate ON cal.calendarsusers_y2017m11 (createdate);
CREATE INDEX calendarsusers_y2017m12_createdate ON cal.calendarsusers_y2017m12 (createdate);

CREATE OR REPLACE FUNCTION calendarsusers_insert_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF ( NEW.createdate >= DATE '2015-10-01' AND
         NEW.createdate < DATE '2015-11-01' ) THEN
        INSERT INTO cal.calendarsusers_y2015m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2015-11-01' AND
            NEW.createdate < DATE '2015-12-01' ) THEN
        INSERT INTO cal.calendarsusers_y2015m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2015-12-01' AND
            NEW.createdate < DATE '2016-01-01' ) THEN
        INSERT INTO cal.calendarsusers_y2015m12 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-01-01' AND
            NEW.createdate < DATE '2016-02-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m01 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-02-01' AND
            NEW.createdate < DATE '2016-03-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m02 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-03-01' AND
            NEW.createdate < DATE '2016-04-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m03 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-04-01' AND
            NEW.createdate < DATE '2016-05-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m04 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-05-01' AND
            NEW.createdate < DATE '2016-06-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m05 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-06-01' AND
            NEW.createdate < DATE '2016-07-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m06 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-07-01' AND
            NEW.createdate < DATE '2016-08-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m07 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-08-01' AND
            NEW.createdate < DATE '2016-09-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m08 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-09-01' AND
            NEW.createdate < DATE '2016-10-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m09 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-10-01' AND
            NEW.createdate < DATE '2016-11-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-11-01' AND
            NEW.createdate < DATE '2016-12-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2016-12-01' AND
            NEW.createdate < DATE '2017-01-01' ) THEN
        INSERT INTO cal.calendarsusers_y2016m12 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-01-01' AND
            NEW.createdate < DATE '2017-02-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m01 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-02-01' AND
            NEW.createdate < DATE '2017-03-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m02 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-03-01' AND
            NEW.createdate < DATE '2017-04-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m03 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-04-01' AND
            NEW.createdate < DATE '2017-05-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m04 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-05-01' AND
            NEW.createdate < DATE '2017-06-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m05 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-06-01' AND
            NEW.createdate < DATE '2017-07-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m06 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-07-01' AND
            NEW.createdate < DATE '2017-08-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m07 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-08-01' AND
            NEW.createdate < DATE '2017-09-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m08 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-09-01' AND
            NEW.createdate < DATE '2017-10-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m09 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-10-01' AND
            NEW.createdate < DATE '2017-11-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m10 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-11-01' AND
            NEW.createdate < DATE '2017-12-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m11 VALUES (NEW.*);
    ELSIF ( NEW.createdate >= DATE '2017-12-01' AND
            NEW.createdate < DATE '2018-01-01' ) THEN
        INSERT INTO cal.calendarsusers_y2017m12 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the calendarsusers_insert_trigger() function!';
    END IF;
    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER insert_calendarsusers_trigger
    BEFORE INSERT ON cal.calendarsusers
    FOR EACH ROW EXECUTE PROCEDURE calendarsusers_insert_trigger();
--
--
--
-- -----------------------------------------------------
-- Table cal."calendaraccess"
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS cal."calendaraccess" (
   idsiteuser INTEGER REFERENCES caluser.siteusers (idsiteuser),
   lastaccess TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE cal."calendaraccess"
OWNER TO caldba;

CREATE TABLE cal.calendaraccess_y2015m10 (
    CHECK ( lastaccess >= DATE '2015-10-01' AND lastaccess < DATE '2015-11-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2015m11 (
    CHECK ( lastaccess >= DATE '2015-11-01' AND lastaccess < DATE '2015-12-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2015m12 (
    CHECK ( lastaccess >= DATE '2015-12-01' AND lastaccess < DATE '2016-01-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m01 (
    CHECK ( lastaccess >= DATE '2016-01-01' AND lastaccess < DATE '2016-02-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m02 (
    CHECK ( lastaccess >= DATE '2016-02-01' AND lastaccess < DATE '2016-03-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m03 (
    CHECK ( lastaccess >= DATE '2016-03-01' AND lastaccess < DATE '2016-04-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m04 (
    CHECK ( lastaccess >= DATE '2016-04-01' AND lastaccess < DATE '2016-05-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m05 (
    CHECK ( lastaccess >= DATE '2016-05-01' AND lastaccess < DATE '2016-06-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m06 (
    CHECK ( lastaccess >= DATE '2016-06-01' AND lastaccess < DATE '2016-07-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m07 (
    CHECK ( lastaccess >= DATE '2016-07-01' AND lastaccess < DATE '2016-08-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m08 (
    CHECK ( lastaccess >= DATE '2016-08-01' AND lastaccess < DATE '2016-09-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m09 (
    CHECK ( lastaccess >= DATE '2016-09-01' AND lastaccess < DATE '2016-10-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m10 (
    CHECK ( lastaccess >= DATE '2016-10-01' AND lastaccess < DATE '2016-11-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m11 (
    CHECK ( lastaccess >= DATE '2016-11-01' AND lastaccess < DATE '2016-12-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2016m12 (
    CHECK ( lastaccess >= DATE '2016-12-01' AND lastaccess < DATE '2017-01-01' )
) INHERITS (cal.calendaraccess);

CREATE TABLE cal.calendaraccess_y2017m01 (
    CHECK ( lastaccess >= DATE '2017-01-01' AND lastaccess < DATE '2017-02-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m02 (
    CHECK ( lastaccess >= DATE '2017-02-01' AND lastaccess < DATE '2017-03-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m03 (
    CHECK ( lastaccess >= DATE '2017-03-01' AND lastaccess < DATE '2017-04-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m04 (
    CHECK ( lastaccess >= DATE '2017-04-01' AND lastaccess < DATE '2017-05-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m05 (
    CHECK ( lastaccess >= DATE '2017-05-01' AND lastaccess < DATE '2017-06-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m06 (
    CHECK ( lastaccess >= DATE '2017-06-01' AND lastaccess < DATE '2017-07-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m07 (
    CHECK ( lastaccess >= DATE '2017-07-01' AND lastaccess < DATE '2017-08-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m08 (
    CHECK ( lastaccess >= DATE '2017-08-01' AND lastaccess < DATE '2017-09-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m09 (
    CHECK ( lastaccess >= DATE '2017-09-01' AND lastaccess < DATE '2017-10-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m10 (
    CHECK ( lastaccess >= DATE '2017-10-01' AND lastaccess < DATE '2017-11-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m11 (
    CHECK ( lastaccess >= DATE '2017-11-01' AND lastaccess < DATE '2017-12-01' )
) INHERITS (cal.calendaraccess);
CREATE TABLE cal.calendaraccess_y2017m12 (
    CHECK ( lastaccess >= DATE '2017-12-01' AND lastaccess < DATE '2018-01-01' )
) INHERITS (cal.calendaraccess);

CREATE INDEX calendaraccess_y2015m10_lastaccess ON cal.calendaraccess_y2015m10 (lastaccess);
CREATE INDEX calendaraccess_y2015m11_lastaccess ON cal.calendaraccess_y2015m11 (lastaccess);
CREATE INDEX calendaraccess_y2015m12_lastaccess ON cal.calendaraccess_y2015m12 (lastaccess);
CREATE INDEX calendaraccess_y2016m01_lastaccess ON cal.calendaraccess_y2016m01 (lastaccess);
CREATE INDEX calendaraccess_y2016m02_lastaccess ON cal.calendaraccess_y2016m02 (lastaccess);
CREATE INDEX calendaraccess_y2016m03_lastaccess ON cal.calendaraccess_y2016m03 (lastaccess);
CREATE INDEX calendaraccess_y2016m04_lastaccess ON cal.calendaraccess_y2016m04 (lastaccess);
CREATE INDEX calendaraccess_y2016m05_lastaccess ON cal.calendaraccess_y2016m05 (lastaccess);
CREATE INDEX calendaraccess_y2016m06_lastaccess ON cal.calendaraccess_y2016m06 (lastaccess);
CREATE INDEX calendaraccess_y2016m07_lastaccess ON cal.calendaraccess_y2016m07 (lastaccess);
CREATE INDEX calendaraccess_y2016m08_lastaccess ON cal.calendaraccess_y2016m08 (lastaccess);
CREATE INDEX calendaraccess_y2016m09_lastaccess ON cal.calendaraccess_y2016m09 (lastaccess);
CREATE INDEX calendaraccess_y2016m10_lastaccess ON cal.calendaraccess_y2016m10 (lastaccess);
CREATE INDEX calendaraccess_y2016m11_lastaccess ON cal.calendaraccess_y2016m11 (lastaccess);
CREATE INDEX calendaraccess_y2016m12_lastaccess ON cal.calendaraccess_y2016m12 (lastaccess);

CREATE INDEX calendaraccess_y2017m01_lastaccess ON cal.calendaraccess_y2017m01 (lastaccess);
CREATE INDEX calendaraccess_y2017m02_lastaccess ON cal.calendaraccess_y2017m02 (lastaccess);
CREATE INDEX calendaraccess_y2017m03_lastaccess ON cal.calendaraccess_y2017m03 (lastaccess);
CREATE INDEX calendaraccess_y2017m04_lastaccess ON cal.calendaraccess_y2017m04 (lastaccess);
CREATE INDEX calendaraccess_y2017m05_lastaccess ON cal.calendaraccess_y2017m05 (lastaccess);
CREATE INDEX calendaraccess_y2017m06_lastaccess ON cal.calendaraccess_y2017m06 (lastaccess);
CREATE INDEX calendaraccess_y2017m07_lastaccess ON cal.calendaraccess_y2017m07 (lastaccess);
CREATE INDEX calendaraccess_y2017m08_lastaccess ON cal.calendaraccess_y2017m08 (lastaccess);
CREATE INDEX calendaraccess_y2017m09_lastaccess ON cal.calendaraccess_y2017m09 (lastaccess);
CREATE INDEX calendaraccess_y2017m10_lastaccess ON cal.calendaraccess_y2017m10 (lastaccess);
CREATE INDEX calendaraccess_y2017m11_lastaccess ON cal.calendaraccess_y2017m11 (lastaccess);
CREATE INDEX calendaraccess_y2017m12_lastaccess ON cal.calendaraccess_y2017m12 (lastaccess);


CREATE OR REPLACE FUNCTION calendaraccess_insert_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF ( NEW.lastaccess >= DATE '2015-10-01' AND
         NEW.lastaccess < DATE '2015-11-01' ) THEN
        INSERT INTO cal.calendaraccess_y2015m10 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2015-11-01' AND
            NEW.lastaccess < DATE '2015-12-01' ) THEN
        INSERT INTO cal.calendaraccess_y2015m11 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2015-12-01' AND
            NEW.lastaccess < DATE '2016-01-01' ) THEN
        INSERT INTO cal.calendaraccess_y2015m12 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-01-01' AND
            NEW.lastaccess < DATE '2016-02-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m01 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-02-01' AND
            NEW.lastaccess < DATE '2016-03-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m02 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-03-01' AND
            NEW.lastaccess < DATE '2016-04-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m03 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-04-01' AND
            NEW.lastaccess < DATE '2016-05-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m04 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-05-01' AND
            NEW.lastaccess < DATE '2016-06-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m05 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-06-01' AND
            NEW.lastaccess < DATE '2016-07-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m06 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-07-01' AND
            NEW.lastaccess < DATE '2016-08-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m07 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-08-01' AND
            NEW.lastaccess < DATE '2016-09-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m08 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-09-01' AND
            NEW.lastaccess < DATE '2016-10-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m09 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-10-01' AND
            NEW.lastaccess < DATE '2016-11-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m10 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-11-01' AND
            NEW.lastaccess < DATE '2016-12-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m11 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2016-12-01' AND
            NEW.lastaccess < DATE '2017-01-01' ) THEN
        INSERT INTO cal.calendaraccess_y2016m12 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-01-01' AND
            NEW.lastaccess < DATE '2017-02-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m01 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-02-01' AND
            NEW.lastaccess < DATE '2017-03-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m02 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-03-01' AND
            NEW.lastaccess < DATE '2017-04-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m03 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-04-01' AND
            NEW.lastaccess < DATE '2017-05-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m04 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-05-01' AND
            NEW.lastaccess < DATE '2017-06-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m05 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-06-01' AND
            NEW.lastaccess < DATE '2017-07-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m06 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-07-01' AND
            NEW.lastaccess < DATE '2017-08-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m07 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-08-01' AND
            NEW.lastaccess < DATE '2017-09-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m08 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-09-01' AND
            NEW.lastaccess < DATE '2017-10-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m09 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-10-01' AND
            NEW.lastaccess < DATE '2017-11-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m10 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-11-01' AND
            NEW.lastaccess < DATE '2017-12-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m11 VALUES (NEW.*);
    ELSIF ( NEW.lastaccess >= DATE '2017-12-01' AND
            NEW.lastaccess < DATE '2018-01-01' ) THEN
        INSERT INTO cal.calendaraccess_y2017m12 VALUES (NEW.*);
    ELSE
        RAISE EXCEPTION 'Date out of range.  Fix the calendaraccess_insert_trigger() function!';
    END IF;
    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER insert_calendaraccess_trigger
BEFORE INSERT ON cal.calendaraccess
FOR EACH ROW EXECUTE PROCEDURE calendaraccess_insert_trigger();
