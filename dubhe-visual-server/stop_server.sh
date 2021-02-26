#!/bin/bash

ps -ef | grep python |grep -v "ndp" |awk '{print $2}'|xargs kill -9

source deactivate

echo '服务停止成功，退出conda虚拟环境'
