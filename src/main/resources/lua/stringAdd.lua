local key = KEYS[1]
local value = KEYS[2]

redis.call('set', key, value)
