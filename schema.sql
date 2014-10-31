-- delete tables and views
DROP VIEW IF EXISTS osquery_processes;
DROP VIEW IF EXISTS osquery_etc_hosts;
DROP TABLE IF EXISTS _osquery_processes;
DROP TABLE IF EXISTS _osquery_etc_hosts;

-- delete stored procedures
DROP PROCEDURE osquery_load_processes;
DROP PROCEDURE osquery_load_etc_hosts;

DROP PROCEDURE osquery_snapshot;

-- create stored procedures
CREATE PROCEDURE osquery_load_processes() AS LANGUAGE JAVA NAME 'cubrid_osquery.osquery_load_processes()';
CREATE PROCEDURE osquery_load_etc_hosts() AS LANGUAGE JAVA NAME 'cubrid_osquery.osquery_load_etc_hosts()';

CREATE PROCEDURE osquery_snapshot() AS LANGUAGE JAVA NAME 'cubrid_osquery.osquery_snapshot()';

-- create tables
CREATE TABLE _osquery_processes (pid INT, name VARCHAR(255));
CREATE TABLE _osquery_etc_hosts (address VARCHAR(50), hostnames VARCHAR(4096));

-- create views
CREATE VIEW osquery_processes AS SELECT * FROM _osquery_processes WHERE osquery_load_processes() IS NULL;
CREATE VIEW osquery_etc_hosts AS SELECT * FROM _osquery_etc_hosts WHERE osquery_load_etc_hosts() IS NULL;
