#!/bin/sh

# parse arguments
if [ "$#" -eq 2 ]; then
	database="$1"
	username="$2"
	password=""
elif [ "$#" -eq 3 ]; then
	database="$1"
	username="$2"
	password="$3"
else
	echo "Bad arguments!"
	echo ""
	echo "Usage:"
	echo "   ./install.sh <database> <sql_user> [<sql_password>]"
	exit 0
fi

# compile
echo " * Compiling Java stored procedure ..."
javac cubrid_osquery.java

# install java class in database
echo " * Installig Java stored procedure in database $database ..."
loadjava $database cubrid_osquery.class 

# clean up database of current instances
echo " * Installing schema in database $database ..."
csql -C -s -u $username -p "$password" -i schema.sql $database
