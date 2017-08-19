from __future__ import division
import os
import glob
import re

list_of_files = glob.glob('*.log')
#print(list_of_files)
maxendtimeall=0
minstarttime=0
i=0
sum1=0


nodes=0
numofreq=100


for fileName in list_of_files:
    f=open(fileName,"r")
    lines=f.readlines()
    max1=0
    nodes=nodes+1
    maxendtime=0

    start=0
    f.close()
    for l in lines:
        
        if "Message Count" in l:
            x=l.split("Message Count:")
            if(int(x[1])>max1):
                max1=int(x[1])
                #print(max1)

        if "System start time:" in l:
            y=l.split("System start time:")
            regex=r"([ a-zA-Z]+):(\d+)ms"
            match = re.search(regex,l)
            start=(int(match.group(2)))
            #print(start)

        if "System end time:" in l:
            z=l.split("System start time:")
            regex=r"([ a-zA-Z]+):(\d+)ms"
            match = re.search(regex,l)
            end1=(int(match.group(2)))

            if(end1>maxendtime):
                maxendtime=end1
                #print(end1)

    if(minstarttime==0):
        minstarttime=start
    elif(start < minstarttime):
        minstarttime=start
    
    if(maxendtime>maxendtimeall):
        maxendtimeall=maxendtime

    sum1=sum1+max1

print('MessageComplexity= %d'%sum1)
#print(minstarttime)
#print(maxendtimeall)
TotalReq=nodes*numofreq
Throughput=(maxendtimeall-minstarttime)/TotalReq
print('Throughput =%f' %Throughput)

os.system("pause")
                

    
            
