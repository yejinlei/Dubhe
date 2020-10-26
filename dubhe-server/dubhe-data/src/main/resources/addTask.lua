local keys,values = KEYS,ARGV
local taskKey = cjson.decode(values[3])..':'..keys[1]..':'..cjson.decode(values[4])
if redis.call('ZADD',cjson.decode(values[1]),values[6],cjson.decode(values[5]))
then redis.call('SET',taskKey,cjson.decode(values[2]))
return true
else return false end
