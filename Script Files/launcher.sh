#!/bin/bash


# Change this to your netid
netid=gxs161530

#
# Root directory of your project
PROJDIR=$HOME/AOS/mutex
#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
#BINDIR= $HOME/AOS/test

# Your main project class
#
PROG=Launcher
n=0
#javac $PROJDIR/$PROG.java
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
	read i
    while read -r line || [[ -n $line ]]; 
    do
		host=$( echo $line | awk '{ print $2 }' )
		domain=$host".utdallas.edu"
        ssh $netid@$domain java -cp $PROJDIR $PROG $n $CONFIG > $PROJDIR/${n}.log &
		
		n=$(( n + 1 ))
    done
   
)