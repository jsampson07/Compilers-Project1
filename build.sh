#!/bin/bash
# Write a script to build your optimizer in this file 
# (As required by your chosen optimizer language)
mkdir -p build
find materials/src -name "*.java" > sources.txt -exec javac -d build -sourcepath materials/src {} + 
# or just /
# find $PWD/materials -name "*.java" > sources.txt

#javac -d build @sources.txt

#how to deal with the space in my JOSHUA SAMPSON