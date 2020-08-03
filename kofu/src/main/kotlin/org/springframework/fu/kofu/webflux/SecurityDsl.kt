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

package org.springframework.fu.kofu.webflux

import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.fu.kofu.AbstractDsl
import org.springframework.fu.kofu.ConfigurationDsl
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.SecurityInitializer
import org.springframework.security.config.annotation.web.reactive.WebFluxSecurityInitializer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.dsl.config.builders.server.ServerHttpSecurityDsl
import org.springframework.security.dsl.config.builders.server.invoke

/**
 * Kofu DSL for spring-security.
 *
 * Configure spring-security.
 *
 * Required dependencies can be retrieve using `org.springframework.boot:spring-boot-starter-security`.
 *
 * @author Jonas Bark, Ivan Skachkov
 */
class SecurityDsl(private val init: SecurityDsl.() -> Unit) : AbstractDsl() {

    var authenticationManager: ReactiveAuthenticationManager? = null

    var reactiveUserDetailsService: ReactiveUserDetailsService? = null

    var passwordEncoder: PasswordEncoder? = null

    var userDetailsPasswordService: ReactiveUserDetailsPasswordService? = null

    var http: ServerHttpSecurityDsl.() -> Unit = {}

    /**
     * For customizations not available through spring-security-dsl
     */
    var securityCustomizer: (http: ServerHttpSecurity) -> ServerHttpSecurity = { it }

    override fun initialize(context: GenericApplicationContext) {
        super.initialize(context)
        init()

        val securityInitializer = SecurityInitializer(
                authenticationManager,
                reactiveUserDetailsService,
                passwordEncoder,
                userDetailsPasswordService
        )
        securityInitializer.initialize(context)

        securityCustomizer.invoke(securityInitializer.httpSecurity)

        val chain = securityInitializer.httpSecurity.invoke(http)

        val webFlux = context is ReactiveWebServerApplicationContext
        if (webFlux) {
            WebFluxSecurityInitializer(chain).initialize(context)
        }
    }
}

/**
 * Configure spring-security.
 *
 * Requires `org.springframework.boot:spring-boot-starter-security` dependency.
 *
 * @sample org.springframework.fu.kofu.samples.securityDsl
 * @author Jonas Bark
 */
fun ConfigurationDsl.security(dsl: SecurityDsl.() -> Unit = {}) {
    SecurityDsl(dsl).initialize(context)
}
