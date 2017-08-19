from __future__ import division
import os
import re
#file = open('responseAvg.txt','w')

with open("/home/012/g/gx/gxs161530/AOS/mutex/respTime.txt") as f: #  D:\\UTD\\AOS\\Project2\\Final\\respTime.txt
	total = 0
	lines=f.readlines()
	count = len(lines)
	for line in lines:
		entry = line.split(" ")[-1];
		val = int(re.sub('[^0-9]','',entry.split(":")[-1]))
		total = total+val	
	
	avg = total / count
	#file.write(str(avg))
	print(str(avg))