# Primitive JS build system; intended to be run from shell
# Watches main.js and automatically rebuilds main-build.js
cd ../src/main/webapp

cat admin_console.js group_editor.js group_manager.js list_input.js loader.js login.js navbar.js permissions_manager.js reservation_creator.js reservation_editor.js reservation_list.js resource_creator.js resource_editor.js resource_list.js router.js settings.js main.js > main-build.js
