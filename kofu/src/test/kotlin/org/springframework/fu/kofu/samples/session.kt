/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.fu.kofu.samples

import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.session.reactiveRedis
import org.springframework.fu.kofu.webflux.security
import org.springframework.fu.kofu.session.session
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.session.SaveMode
import java.time.Duration

fun sessionDsl() {
    reactiveWebApplication {
        session {
            reactiveRedis {
                maxInactiveIntervalInSeconds = Duration.ofSeconds(Long.MAX_VALUE)
                redisNamespace = "spring:session" // default
                saveMode = SaveMode.ON_SET_ATTRIBUTE // default
            }
        }
        webFlux {
            security {
                http {
                    anonymous {  }
                    authorizeExchange {
                        authorize("/view", hasRole("USER"))
                        authorize("/public-view", permitAll)
                    }
                    headers {}
                    logout {}
                }
            }
        }
    }
}
