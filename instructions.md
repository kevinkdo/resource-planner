# PostgreSQL Instructions (linux instructions)

## Install PostgreSQL 9.5 

$ sudo apt-get update
$ sudo apt-get install postgresql postgresql-contrib

## Login to postgres account

$ sudo -i -u postgres

## Create test database with name rp

$ createdb rp

## Connect to test database rp

$ psql rp

## Create necessary tables using setup.sql found in this directory

Instructions to follow

# Gradle Instructions

## Install Java 8

Figure it out

## Install homebrew

$ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

## Install gradle

brew install gradle

# Build and run instructions

Navigate to directory containing build.gradle

$ gradle build
$ gradle wrapper
$ ./gradlew bootRun

App should now be running on localhost:8080