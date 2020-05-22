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
package io.emeraldpay.dshackle

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.text.SimpleDateFormat
import java.util.*

class Global {

    companion object {

        @JvmStatic
        val objectMapper: ObjectMapper = createObjectMapper()

        private fun createObjectMapper(): ObjectMapper {
            val module = SimpleModule("EmeraldDshackle", Version(1, 0, 0, null, null, null))

            val objectMapper = ObjectMapper()
            objectMapper.registerModule(module)
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper
                    .setDateFormat(SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS"))
                    .setTimeZone(TimeZone.getTimeZone("UTC"))

            return objectMapper
        }

    }

}