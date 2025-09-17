#!/bin/bash

# Write a script to build your optimizer in this file 
# (As required by your chosen optimizer language)

mkdir -p build
find src -name "*.java" > sources.txt
javac -d build @sources.txt