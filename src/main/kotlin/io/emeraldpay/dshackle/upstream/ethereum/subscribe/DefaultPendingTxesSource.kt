/**
 * Copyright (c) 2022 EmeraldPay, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.dshackle.upstream.ethereum.subscribe

import io.emeraldpay.dshackle.commons.DurableFlux
import io.emeraldpay.dshackle.commons.SharedFluxHolder
import io.emeraldpay.dshackle.upstream.Selector
import io.emeraldpay.dshackle.upstream.SubscriptionConnect
import io.emeraldpay.etherjar.domain.TransactionId
import reactor.core.publisher.Flux
import java.time.Duration

abstract class DefaultPendingTxesSource : SubscriptionConnect<TransactionId>, PendingTxesSource {

    private val connectionSource = DurableFlux
        .newBuilder()
        .using(::createConnection)
        .backoffOnError(Duration.ofMillis(100), 1.5, Duration.ofSeconds(60))
        .build()
    private val holder = SharedFluxHolder<TransactionId>(
        connectionSource::connect,
    )

    override fun connect(matcher: Selector.Matcher): Flux<TransactionId> {
        return holder.get()
    }

    abstract fun createConnection(): Flux<TransactionId>
}
