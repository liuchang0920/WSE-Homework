#!/bin/bash

number=$1
prefix='https://commoncrawl.s3.amazonaws.com/'

count=0
for file in `cat wet.paths`
do
	wget $prefix$file
	if [ $count -eq $number ]
	then
		break
	fi
	count=$(($count+1))
done