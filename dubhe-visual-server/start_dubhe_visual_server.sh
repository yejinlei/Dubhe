#!/bin/sh

SERVER=$(systemctl is-active server.service)
DJANGO=$(systemctl is-active django@$1.service)
MONITOR=$(systemctl is-active monitor.service)

if [ $SERVER = active  ]
then 
	echo restart server
	systemctl restart server.service
else 
	echo start server
	systemctl start server.service
fi

if [ $DJANGO = active  ]
then
	echo restart django at port $1
	systemctl restart django@$1.service
else 
	echo start django at port $1
	systemctl start django@$1.service
fi

if [ $MONITOR = active  ]
then 
echo restart monitor
systemctl restart monitor.service
else 
echo start monitor
systemctl start monitor.service
fi

