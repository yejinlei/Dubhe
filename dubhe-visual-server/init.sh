#!/bin/bash
wget https://repo.anaconda.com/archive/Anaconda3-2020.07-Linux-x86_64.sh
bash Anaconda3-2020.07-Linux-x86_64.sh
echo 'export PATH=$PATH:/root/anaconda3/bin'>>/root/.bashrc

source /root/.bashrc

conda env create --file dubhe_visual.yaml

echo '环境初始化成功，启动服务请执行：source start_server.sh'

