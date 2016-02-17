# Primitive JS build system; intended to be run from shell
# Builds main-build.js once
babel --presets react ../src/main/webapp/main.js -o ../src/main/webapp/main-build.js