#!/usr/bin/env bash

# fetch first 100 lines
i = 0
while read line; do
    echo $line
done <"../data/wet-list/wet.paths"
