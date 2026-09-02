local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local tokensToConsume = tonumber(ARGV[3])
local ttlMillis = tonumber(ARGV[4])

-- Use Redis as the single source of time for all API instances.
local redisTime = redis.call('TIME')
local nowMillis =
(tonumber(redisTime[1]) * 1000)
        + math.floor(tonumber(redisTime[2]) / 1000)

-- Read the current bucket state.
local bucket = redis.call(
        'HMGET',
        key,
        'tokens',
        'last_refill'
)

local currentTokens = tonumber(bucket[1])
local lastRefill = tonumber(bucket[2])

-- A bucket that does not exist starts full.
if currentTokens == nil or lastRefill == nil then
    currentTokens = capacity
    lastRefill = nowMillis
else
    local elapsedMillis = math.max(0, nowMillis - lastRefill)

    local tokensToAdd =
    (elapsedMillis / 1000.0) * refillRate

    currentTokens =
    math.min(capacity, currentTokens + tokensToAdd)
end

local allowed = 0
local retryAfterSeconds = 0

if currentTokens >= tokensToConsume then
    currentTokens = currentTokens - tokensToConsume
    allowed = 1
else
    local missingTokens = tokensToConsume - currentTokens

    retryAfterSeconds =
    math.ceil(missingTokens / refillRate)
end

-- Persist the updated bucket.
redis.call(
        'HSET',
        key,
        'tokens', tostring(currentTokens),
        'last_refill', tostring(nowMillis)
)

-- Remove inactive buckets automatically.
redis.call('PEXPIRE', key, ttlMillis)

return {
    allowed,
    math.floor(currentTokens),
    retryAfterSeconds
}