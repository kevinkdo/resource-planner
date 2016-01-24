# Setup Instructions (linux/vagrant instructions)
The directory containing Vagrantfile will now be shared from your computer and the vm. You can access it in the vm by going to the folder 'Vagrant'. Go up two levels so that when you hit ls it should read 'vagrant@vagrant-ubuntu-trusty-64:/$'

In Mac terminal:
```
git clone https://github.com/kevinkdo/resource-planner.git
cd resource-planner
vagrant up 
vagrant ssh
```

In vagrant shell: 
```
$ chmod 755 setupvm.sh
$ ./setupvm.sh
```

Setting up DB:
```
$ sudo -u postgres -i
$ createdb rp
$ psql rp
$ ***Run setup.sql file. You can just copy paste it into the psql utility***
$ ***Run following two commands to test it worked. You should see all 4 tables and the 1 default admin user***
$ \dt
$ SELECT * FROM user;
$ *** Now we return to normal shell user ***
$ su vagrant (password should be 'vagrant')
$ *** Finally, change directory to normal vagrant directory***
```

# Run
```
$ chmod 755 run.sh
$ ./run.sh
```
App should now be running on localhost:8080
