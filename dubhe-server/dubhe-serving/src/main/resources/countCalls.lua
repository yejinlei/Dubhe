local args = KEYS
local result = {}
local totalCallCount = 0;
local totalFailedCount = 0;
for key, value in ipairs(args) do
    -- 判断key是否存在
    local key_exists = redis.call("exists", "serving:inference:metrics:" .. value)
    if (key_exists == 1) then
        -- 获取调用次数和失败次数
        local callCount = redis.call("hget", "serving:inference:metrics:" .. value, "callCount")
        totalCallCount = totalCallCount + callCount
        local failedCount = redis.call("hget", "serving:inference:metrics:" .. value, "failedCount")
        if (failedCount ~= false) then
            totalFailedCount = totalFailedCount + failedCount
        end
    end
end
result[1] = totalCallCount
result[2] = totalFailedCount
return result;