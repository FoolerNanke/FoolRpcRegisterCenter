local key = KEYS[1]
local value = KEYS[2]
local expireTime = KEYS[3]
--- 插入数据
local res = redis.call('SISMEMBER', key, value)
if res == 0 then
    local call = redis.call('sadd', key, value)
    if call == 0 then
        return "0"
    end
end
if expireTime > 0 then
    redis.expire(key, expireTime)
end
return "1"
--- 返回值类型为Integer