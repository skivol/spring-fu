/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.fu.kofu.webflux

import org.junit.jupiter.api.Test
import org.springframework.fu.kofu.basicAuth
import org.springframework.fu.kofu.localServerPort
import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.repoAuthenticationManager
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration


/**
 * @author Jonas Bark
 * @author Ivan Skachkov
 */
class SecurityDslTests {

	@Test
	fun `Check spring-security configuration DSL`() {
		val app = reactiveWebApplication {
			security {
				authenticationManager = repoAuthenticationManager()

				http = {
					authorizeExchange {
						authorize("/public-view", permitAll)
						authorize("/view", hasRole("USER"))
					}
					httpBasic {}
				}
			}
			webFlux {
				port = 0
				router {
					GET("/public-view") { ok().build() }
					GET("/view") { ok().build() }
				}
			}
		}
		with(app.run()) {
			val client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:$localServerPort")
					.responseTimeout(Duration.ofMinutes(10)) // useful for debug
					.build()

			client.get().uri("/public-view").exchange()
					.expectStatus().is2xxSuccessful

			client.get().uri("/view").exchange()
					.expectStatus().isUnauthorized

			client.get().uri("/view").header("Authorization", "Basic ${basicAuth()}").exchange()
					.expectStatus().is2xxSuccessful

			close()
		}
	}
}
