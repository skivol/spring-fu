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

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.fu.kofu.*
import org.springframework.fu.kofu.redis.reactiveRedis
import org.springframework.fu.kofu.session.reactiveRedis
import org.springframework.fu.kofu.session.session
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.session.ReactiveSessionRepository
import org.springframework.session.Session
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters.fromFormData
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.testcontainers.containers.GenericContainer
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * @author Ivan Skachkov
 */
class SessionDslTests {

    private lateinit var redis: GenericContainer<Nothing>

    @BeforeAll
    fun setup() {
        redis = object : GenericContainer<Nothing>("redis:5") {
            init {
                withExposedPorts(6379)
            }
        }
        redis.start()
    }

    @Test
    fun `Check spring-session configuration DSL`() {
        val app = reactiveWebApplication {
            reactiveRedis {
                port = redis.firstMappedPort
                lettuce()
            }
            session {
                reactiveRedis()
            }
            webFlux {
                port = 0
                router {
                    GET("/view") { ok().build() }
                    GET("/login") { ok().build() }
                }
                codecs {
                    form()
                }
                filter<CsrfFilter>()

                security {
                    authenticationManager = repoAuthenticationManager()

                    http {
                        authorizeExchange {
                            authorize("/view", hasRole("USER"))
                            authorize("/login", permitAll)
                        }
                        formLogin {
                            loginPage = "/login"
                        }
                        csrf {
                            csrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse()
                        }
                        logout { }
                    }
                }
            }
        }

        with(app.run()) {
            use {
                val client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:$localServerPort")
                        .responseTimeout(Duration.ofMinutes(10)) // useful for debug
                        .build()
                // Simulating browser - get csrf token
                val getLoginResult = client.get().uri("/login")
                        .exchange()
                        .expectStatus().is2xxSuccessful
                        .expectHeader().exists(SET_COOKIE)
                        .expectBody().returnResult()

                // Example response header -> Set-Cookie: [XSRF-TOKEN=247d87a6-f5d4-4d3a-96f6-f99e14869cd7; Path=/]
                val csrfValue = getLoginResult.responseCookies.getFirst("XSRF-TOKEN")!!.value
                // Try logging in
                val postLoginResult = client.post().uri("/login")
                        .header("X-XSRF-TOKEN", csrfValue)
                        .cookie("XSRF-TOKEN", csrfValue)
                        .body(fromFormData(SPRING_SECURITY_FORM_USERNAME_KEY, username).with(SPRING_SECURITY_FORM_PASSWORD_KEY, password))
                        .exchange()
                        .expectStatus().is3xxRedirection
                        .expectHeader().exists(SET_COOKIE)
                        .expectBody().returnResult()

                // Example header -> Set-Cookie: [SESSION=d07c0ebc-82a3-40d6-aeda-d778fa3e9ae1; Path=/; HttpOnly; SameSite=Lax]
                val session = postLoginResult.responseCookies.getFirst("SESSION")?.value
                assert(session != null) { "Expected session cookie" }

                client.get().uri("/view")
                        .cookie("SESSION", session!!)
                        .exchange()
                        .expectStatus().is2xxSuccessful

                val reactiveRedisSessionRepository = getBean(ReactiveSessionRepository::class.java)
                val redisSessionValue: Session? = reactiveRedisSessionRepository.findById(session).block()
                assert(redisSessionValue != null) { "Expected redis session" }
                assert(!redisSessionValue!!.isExpired) { "Expected not expired session" }

                // Try logging out and check that session is gone from redis
                val postLogoutResult = client.post().uri("/logout")
                        .header("X-XSRF-TOKEN", csrfValue)
                        .cookie("XSRF-TOKEN", csrfValue)
                        .cookie("SESSION", session)
                        .exchange()
                        .expectStatus().is3xxRedirection
                        .expectHeader().exists(SET_COOKIE)
                        .expectBody().returnResult()
                // New session is assigned after logout
                // Example response cookie -> Set-Cookie: [XSRF-TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT, SESSION=57fc7f02-7411-4a36-8510-d2abef8eb132; Path=/; HttpOnly; SameSite=Lax]
                val sessionIdAfterLogout = postLogoutResult.responseCookies.getFirst("SESSION")!!.value
                assert(sessionIdAfterLogout != session) { "Expected session to be different after logout" }

                // Old session should be gone from redis
                val redisSessionValueAfterLogout: Session? = reactiveRedisSessionRepository.findById(session).block()
                assert(redisSessionValueAfterLogout == null) { "Expected redis session to be gone after logout" }
            }
        }
    }

    @AfterAll
    fun tearDown() {
        redis.stop()
    }
}

class CsrfFilter : WebFilter { // https://github.com/spring-projects/spring-security/issues/5766
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return (exchange.getAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name) ?: Mono.empty())
                .doOnSuccess {} // do nothing, just subscribe :/
        .then(chain.filter(exchange))
    }
}
