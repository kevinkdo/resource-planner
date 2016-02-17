# Primitive JS build system; intended to be run from shell
# Watches main.js and automatically rebuilds main-build.js
babel --presets react ../src/main/webapp/main.js --watch -o ../src/main/webapp/main-build.js