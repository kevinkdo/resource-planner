#!/bin/bash
# run 'chmod 755 setup' prior to being able to run script
sudo apt-get update
sudo apt-get install postgresql postgreseql-contrib

#install java
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default

#install gradle
sudo add-apt-repository ppa:cwchien/gradle
sudo apt-get update
sudo apt-get install gradle
