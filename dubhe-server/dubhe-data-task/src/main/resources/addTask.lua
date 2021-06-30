local keys,values = KEYS,ARGV
if redis.call('ZADD',cjson.decode(values[1]),values[3],keys[1])
then redis.call('SET',keys[1],cjson.decode(values[2]))
return true
else return false end
