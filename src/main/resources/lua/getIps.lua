local className = KEYS[1]
local expireSet = KEYS[2]

--- 获取class下的ip列表与过期ip的交集
local diff = redis.call('sinter', className, expireSet)
--- 逐一删除过期ip
for i, ele in ipairs(diff) do
    redis.call('srem', className, ele)
end
--- 返回有效ip
return redis.call('smembers', className);