#!/bin/bash

# Use API to populate
# python Evolution1Setup.py

# Direct psql populate
psql -h localhost -d rp -U postgres -f ev1.sql

# do this to dump the database to file
# pg_dump rp -U postgres -h localhost -F c --file=ev1.sql
