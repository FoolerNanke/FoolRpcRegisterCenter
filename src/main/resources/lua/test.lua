--- 获取key
local key = KEYS[1]
--- 获取value
local val = KEYS[2]
--- 获取一个参数
local expire = ARGV[1]
--- 如果redis找不到这个key就去插入
redis.call("set", key, val)