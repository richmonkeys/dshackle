/**
 * Copyright (c) 2020 EmeraldPay, Inc
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
package io.emeraldpay.dshackle.upstream.rpcclient

import io.emeraldpay.etherjar.rpc.RpcException

open class JsonRpcException(
    val id: JsonRpcResponse.Id,
    val error: JsonRpcError,
    val upstreamId: String? = null,
    writableStackTrace: Boolean = true,
    cause: Throwable? = null,
) : Exception(error.message, cause, true, writableStackTrace) {

    constructor(id: Int, message: String) : this(JsonRpcResponse.NumberId(id), JsonRpcError(-32005, message))

    constructor(id: Int, message: String, cause: Throwable) : this(JsonRpcResponse.NumberId(id), JsonRpcError(-32005, message), cause = cause)

    companion object {
        fun from(err: RpcException): JsonRpcException {
            val id = err.details?.let {
                if (it is JsonRpcResponse.Id) {
                    it
                } else {
                    JsonRpcResponse.NumberId(-3)
                }
            } ?: JsonRpcResponse.NumberId(-4)
            return JsonRpcException(
                id,
                JsonRpcError.from(err),
            )
        }
    }
}
