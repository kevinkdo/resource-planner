# Primitive JS build system; intended to be run from shell
# Watches main.js and automatically rebuilds main-build.js
cd ../src/main/webapp

if [ -e "main-build.js" ]
    then
        rm main-build.js
    fi
if [ ! -e "main-concat.js" ]
    then
        touch main-concat.js
    fi

babel --presets react main-concat.js --watch -o main-build.js &

function build {
    cat group_editor.js group_manager.js list_input.js loader.js login.js navbar.js permissions_manager.js reservation_creator.js reservation_editor.js reservation_list.js resource_creator.js resource_editor.js resource_list.js router.js settings.js main.js > main-concat.js
    #babel --presets react main-concat.js -o main-build.js
    echo "Hit Enter to rebuild frontend"
}

while true; do
  build
  read
done
