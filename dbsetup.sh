#!/bin/bash

cd setup
#python Evolution1Setup.py
psql -h localhost -d rp -U postgres -f ev1.sql
cd ..