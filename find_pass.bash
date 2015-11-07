#!/bin/bash
touch nochange.list
for i in `cat roll.list`
do
	bash forti_login ${i} ${i} | grep "Logged in."
	if [ $? -eq 0 ]; then
		echo ${i} >> nochange.list
	else
		echo "trying roll number ${i}"
	fi
done
