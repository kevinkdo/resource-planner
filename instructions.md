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
$ ***Run following two commands to test it worked. You should see all 4 tables and the 1 default admin user***
$ \dt
$ SELECT * FROM user;
$ ***Next, we want to make sure that your postgres password matches what is listed in our Spring code***
$ ALTER USER Postgres WITH PASSWORD 'password';
$ *** Now we return to normal shell user ***
$ \q
$ su vagrant (password should be 'vagrant')
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
The build system should have Babel and React CLI tools installed:
```
sudo npm install --global babel-cli
npm install babel-preset-react
```

Then you can run `resource-planner/src/main/webapp/m` to build `main.js` and output `main-build.js`, which is used in the production webapp at `index.html`

# Automated Tests
`cd src/test` and then one of the following commands:

1. To run all tests verbosely `python -m unittest discover -v`
2. To run all tests non-verbosely `python -m unittest discover`
3. To run an individual test `python nameoftest.py -v`
