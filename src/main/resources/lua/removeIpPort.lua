local channel_id = KEYS[1]
local expireSet = KEYS[2]

--- 获取ip_port
local ip_port = redis.call('get', channel_id)
if ip_port ~= false then
    local is = redis.call('SISMEMBER', expireSet, ip_port)
    --- 不存在
    if is == 0 then
        local add = redis.call('sadd', expireSet, ip_port)
        if add == 0 then
            return false
        end
    end
    local res = redis.call('del', channel_id)
    if res == 0 then
        return false
    end
end
return true
