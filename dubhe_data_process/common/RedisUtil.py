"""
/**
* Copyright 2020 Tianshu AI Platform. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* =============================================================
*/
"""
# coding:utf-8
import redis
import sys


def getRedisConnection(host, port, db, password):
    return redis.Redis(host=host, port=port, db=db, password=password)


def getOneMinScoreElement(f, queue):
    return f.zrangebyscore(queue, 0, sys.maxsize, 0, 1)


def deleteElement(f, queue, element):
    f.zrem(queue, element)

# get bu key
def getByKey(f, key):
   print(key)
   return f.get(key);


def pushToQueue(f, key, value):
    f.rpush(key, value)
