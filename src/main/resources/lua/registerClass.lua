local className = KEYS[1]
local ip_port = KEYS[2]
local channel_id = KEYS[3]
local expireSet = KEYS[4]
local expireTime = KEYS[5]


--- 插入第一条数据 存储 app:class
--- 如果该数据存在 则不插入
local res = redis.call('SISMEMBER', className, ip_port)
--- 不存在
if res == 0 then
    local call = redis.call('sadd', className, ip_port)
    if call == 0 then
        --- 插入失败
        return "0"
    end
end

--- 插入第二条数据 存储 channel:ip_port
call = redis.call('set', channel_id, ip_port)
if call == 0 then
    --- 插入失败
    return "0"
end

--- 修改失效队列中的数据
res = redis.call('SISMEMBER', expireSet, ip_port)
if res == 1 then
    --- 存在 需要删除
    call = redis.call('SREM', expireSet, ip_port)
    if call == 0 then
        --- 移除失败
        return "0"
    end
end

if expireTime > 0 then
    redis.call('expire', className, expireTime)
end





