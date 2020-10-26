local picKey = cjson.decode(ARGV[3])..KEYS[1]
redis.call('zrem',cjson.decode(ARGV[1]),cjson.decode(ARGV[4]))
redis.call('del',picKey)
redis.call('zadd',cjson.decode(ARGV[2]),10,cjson.decode(ARGV[4]))