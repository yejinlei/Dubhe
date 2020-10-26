local key = cjson.decode(ARGV[2])..cjson.decode(ARGV[3])
if redis.call('lrem',KEYS[1],1,cjson.decode(ARGV[1]))
then return redis.call('del',key)
end