#!/bin/bash
touch nochange.list
echo "create"
for i in `cat roll.list`
do
	echo "start"
	echo ${i}
	bash forti_login ${i} "nitc1234" | grep "Logged in."
	if [ $? -eq 0 ]; then
		echo "s"
		echo ${i} >> nochange.list
	else
		echo "f"
	fi
	echo "next"
done
echo "end"
