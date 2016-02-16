# Setup Instructions (linux/vagrant instructions)
This directory (containing Vagrantfile) will now be shared between your host computer and the VM's filesystem (at `/vagrant`)

In Mac terminal:
```
git clone https://github.com/kevinkdo/resource-planner.git
cd resource-planner
vagrant up 
vagrant ssh
```

In vagrant shell: 
```
$ chmod +x setupvm.sh
$ ./setupvm.sh
```

Setting up DB:
```
$ sudo -u postgres -i
$ createdb rp
$ psql rp < setup.sql
$ psql rp
rp=# ***Run following two commands from a psql rp shell to test it worked. You should see all 4 tables and the 1 default admin user***
rp=# \dt
rp=# SELECT * FROM users;
rp=# ***Next, we want to make sure that your postgres password matches what is listed in our Spring code***
rp=# ALTER USER Postgres WITH PASSWORD 'password';
rp=# *** Now we return to normal shell user ***
rp=# \q
$ # Now we exit the postgres user
$ <CTRL-D>
```

# Run
```
$ ch /vagrant
$ ./run.sh
```
App should now be running on the VM's port 443 (forwarded to the host's localhost:8443)

# Running the dev webapp
[https://127.0.0.1:8443/dev.html](https://127.0.0.1:8443/dev.html)

# Building the webapp for production
Follow the frontend section of `setupvm.sh` (it might be commented out).

Then you can run `resource-planner/src/main/webapp/m1` to build `main.js` and output `main-build.js`, which is used in the production webapp at `index.html`. If you're developing the frontend, then you may want `resource-planner/src/main/webapp/m`, which starts a daemon that watches `main.js` and automatically rebuilds `main-build.js` when it sees changes.

# Scripts for Simplicity

```
./dbsetup.sh         # imports some data into the database for testing
./frontendrebuilt.sh # rebuilds frontend for production
./run                # builds and runs
```

# Automated Tests

## Automatic testing

```
chmod +x test.sh # set permissions
./test.sh        # run from resource-planner directory
```

## Manual testing

`cd src/test` and then one of the following commands:

1. To run all tests verbosely `python -m unittest discover -v`
2. To run all tests non-verbosely `python -m unittest discover`
3. To run an individual test `python nameoftest.py -v`
