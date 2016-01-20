# Setup Instructions (linux/vagrant instructions)
## The directory containing Vagrantfile will now be shared from your computer and the vm. You can access it in the vm by going to the folder 'Vagrant'

cd into directory with Vagrantfile

# Setup vagrant
```
$ vagrant up
$ vagrant ssh
```

# Setup VM 
```
$ chmod 755 setupvm.sh
$ ./setupvm.sh
```

# Run
```
$ chmod 755 run.sh
$ ./run.sh
```
App should now be running on localhost:8080
