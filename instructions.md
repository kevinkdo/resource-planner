# Setup Instructions (linux/vagrant instructions)
The directory containing Vagrantfile will now be shared from your computer and the vm. You can access it in the vm by going to the folder 'Vagrant'.

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

# Run
```
$ chmod 755 run.sh
$ ./run.sh
```
App should now be running on localhost:8080
