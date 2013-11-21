#!/bin/sh

# preinstall
# 
#
# Created by T. Moskun on 10/23/13.
# Copyright 2013 ezcode. All rights reserved.
java=$(which java | sed 's|/bin/.*||')
if [ "x$java" = "x" ];then
	echo "Java is not installed"
	$(exit 1)
	java_version=$(java -version 2>&1 |head -n 1 | cut -d\" -f 2 | sed -E 's|^([0-9])\.([0-9])(\..+)?$|\2|' | bc)
	if [ $java_version -lt 6 ];then
		echo "Java 7 is not installed"
		$(exit 1)
	fi
fi
$(exit 0)
