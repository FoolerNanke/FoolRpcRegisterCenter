local redis_app_app_version = KEYS[1]
local class_version = KEYS[2]
local class_version_time = KEYS[3]

local redis_class = KEYS[4]
local class_version = KEYS[5]
local app_version = KEYS[6]

local redis_ip_list_app_version = KEYS[7]
local ip_port = KEYS[8]

local redis_channel = KEYS[9]
local channel_id = KEYS[10]
local ip_port = KEYS[11]

--- 插入第一条数据 存储 app:class
--- 如果该数据存在 则不插入
local res = redis.call('HGET', redis_app_app_version, class_version)
if res == false then
    redis.call('HSET', redis_app_app_version, class_version, class_version_time)
end

--- 插入第二条数据 存储 class:app
res = redis.call('HGET', redis_class, class_version)
if res == false then
    redis.call('HSET', redis_class, class_version, app_version)
end

--- 插入第三条数据 存储数据到 app_ipList
res = redis.call('SISMEMBER', redis_ip_list_app_version, ip_port)
if res == 0 then
    redis.call('SADD', redis_ip_list_app_version, ip_port)
end

--- 插入第四条数据 channel_ipPort 存储
res = redis.call('HGET', redis_channel, channel_id)
if res == false then
    redis.call('HSET', redis_channel, channel_id, ip_port)
end





