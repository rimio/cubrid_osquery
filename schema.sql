-- delete tables and views
DROP VIEW IF EXISTS osquery_processes;
DROP TABLE IF EXISTS _osquery_processes;

-- delete stored procedures
DROP PROCEDURE osquery_load_processes;

-- create stored procedures
CREATE PROCEDURE osquery_load_processes() AS LANGUAGE JAVA NAME 'cubrid_osquery.osquery_load_processes()';

-- create tables
CREATE TABLE _osquery_processes (pid INT, name VARCHAR(255));

-- create views
CREATE VIEW osquery_processes AS SELECT * FROM _osquery_processes WHERE osquery_load_processes() IS NULL;
