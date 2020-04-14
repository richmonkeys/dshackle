package io.emeraldpay.dshackle.cache

import com.fasterxml.jackson.databind.ObjectMapper
import io.emeraldpay.dshackle.data.BlockContainer
import io.emeraldpay.dshackle.data.TxContainer
import io.emeraldpay.dshackle.data.TxId
import io.emeraldpay.dshackle.reader.Reader
import io.emeraldpay.grpc.Chain
import io.infinitape.etherjar.rpc.json.TransactionJson
import io.lettuce.core.api.reactive.RedisReactiveCommands
import org.apache.commons.codec.binary.Base64
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Cache transactions in Redis, up to 24 hours.
 */
class TxRedisCache(
        private val redis: RedisReactiveCommands<String, String>,
        private val chain: Chain,
        private val objectMapper: ObjectMapper
) : Reader<TxId, TxContainer> {

    companion object {
        private val log = LoggerFactory.getLogger(TxRedisCache::class.java)

        // max caching time is 24 hours
        private const val MAX_CACHE_TIME_HOURS = 24L
    }

    override fun read(key: TxId): Mono<TxContainer> {
        return redis.get(key(key))
                .map { data ->
                    val json = data
                    val tx = objectMapper.readValue(json, TransactionJson::class.java)
                    TxContainer.from(tx, objectMapper)
                }.onErrorResume {
                    Mono.empty()
                }
    }

    fun evict(block: BlockContainer): Mono<Void> {
        return Mono.just(block)
                .map { block ->
                    block.transactions.map {
                        key(it)
                    }.toTypedArray()
                }.flatMap { keys ->
                    redis.del(*keys)
                }.then()
    }

    fun evict(id: TxId): Mono<Void> {
        return Mono.just(id)
                .flatMap {
                    redis.del(key(it))
                }
                .then()
    }

    fun add(tx: TxContainer, block: BlockContainer): Mono<Void> {
        if (tx.blockId == null || block.hash == null || tx.blockId != block.hash || block.timestamp == null) {
            return Mono.empty()
        }
        return Mono.just(Tuples.of(tx, block))
                .flatMap {
                    val data = String(it.t1.json!!)
                    //default caching time is age of the block, i.e. block create hour ago
                    //keep for hour, but block create 10 seconds ago cache for 10 seconds, as it
                    //still can be replaced in the blockchain
                    val age = Instant.now().epochSecond - it.t2.timestamp!!.epochSecond
                    val ttl = min(age, TimeUnit.HOURS.toSeconds(MAX_CACHE_TIME_HOURS))
                    redis.setex(key(it.t1.hash), ttl, data)
                }
                .doOnError {
                    log.warn("Failed to save to Redis: ${it.message}")
                }
                //if failed to cache, just continue without it
                .onErrorResume {
                    Mono.empty()
                }
                .then()
    }

    /**
     * Key in Redis
     */
    fun key(hash: TxId): String {
        return "tx:${chain.id}:${hash.toHex()}"
    }
}