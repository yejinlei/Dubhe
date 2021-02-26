#!/bin/bash

source activate dubhe_visual
echo '进入conda虚拟环境，正在启动服务...'
DIRNAME=$(pwd)

cd backend/
nohup python $DIRNAME/backend/main.py --port 9898 >django.log 2>&1 &
echo 'http 服务启动'

cd ../parser_service/
nohup python $DIRNAME/parser_service/master.py >server.log 2>&1 &
echo 'parser 服务启动'

cd ../service_utils/
nohup python $DIRNAME/service_utils/monitor.py >monitor.log 2>&1 &
echo 'monitor 服务启动'

echo '服务启动成功，停止服务请执行：source stop_server.sh'
