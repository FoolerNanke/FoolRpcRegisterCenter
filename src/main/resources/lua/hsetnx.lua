local mapKey = KEYS[1]
local key = KEYS[2]
local value = KEYS[3]
local timeExpire = KEYS[4];

--- 如果该数据存在 则不插入
local res = redis.call('hget', mapKey, key)
if res == false then
    local call = redis.call('hset', mapKey, key, value)
    if call == 0 then
        return "0"
    end
end
if timeExpire > 0 then
    redis.expire(mapKey, timeExpire)
end
--- 存在则默认返回 1
return "1"
--- 返回值类型为 Integer