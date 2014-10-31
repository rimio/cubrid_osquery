cubrid_osquery
==============

cubrid_osquery is a port of facebook/osquery for CUBRID. The project aims to support as much as possible from the osquery capabilities but using only CUBRID's Java stored procedures and a bit of schema hackery.

## What is supported so far

The following views are available for use:
* **osquery_processes** - running process list
* **osquery_etc_hosts** - hostname mappings

## The internals

Each view is based on a table with the same schema and it's name prefixed with "\_" (underscore). For example, the **osquery_etc_hosts** view will select from the **_osquery_etc_hosts** table.

Also, for each table there is a related stored procedure named **osquery_load_\<table_name\>** (e.g. **osquery_load_etc_hosts**) that will populate the table with up-to-date information.

Selecting from the table **will use the data already existing there**.

However, the view definition uses an always-true filter predicate (which is not detected as such by the optimizer - yay) which calls the stored procedure. The stored procedure call is marked as uncorrelated so it's execution is done exactly once. For example, **osquery_etc_hosts** is defined as "*SELECT * FROM \_osquery_etc_hosts WHERE osquery_load_etc_hosts() IS NULL*".

Selecting from the view **will generate new data in the related table, and will return the new data**.

**If osquery views are heavily used** consider using the underlying tables as well as the **osquery_snapshot** method which populates all tables at once.

## Installation

Use the `install.sh` script for automatic installation. Please provide the script with the database name as well as a user/password with appropriate privileges (DBA ftw).

Manually, you can compile `cubrid_osquery.java` with `javac`, install the class via CUBRID's `loadjava` and execute the contents of `schema.sql`.
