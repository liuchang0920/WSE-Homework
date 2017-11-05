#!/usr/bin/env bash
# sort -t$'\t'  -k 1,1 -k 2n,2  ../data/inverted-intermediate/temp.txt > ../data/inverted-intermediate/temp-sort.txt

#sort -t$'\t'  -k 1,1 -k 2n,2  "/media/liuchang/HardDrive/study/wse/hw2-data/inverted-intermediate/temp.txt" > "/media/liuchang/HardDrive/study/wse/hw2-data/inverted-intermediate/temp-sorted.txt"

sort -t$'\t'  -k 1,1 -k 2n,2  "/media/liuchang/HardDrive/study/wse/hw3-data/inverted-intermediate/temp.txt" > "/media/liuchang/HardDrive/study/wse/hw3-data/inverted-intermediate/temp-sorted.txt"
