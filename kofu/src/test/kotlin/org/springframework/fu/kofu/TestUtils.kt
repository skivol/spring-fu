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

package org.springframework.fu.kofu

import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import java.nio.charset.Charset
import java.util.*

fun userDetailsService(username: String, password: String): MapReactiveUserDetailsService {
    @Suppress("DEPRECATION")
    val user = User.withDefaultPasswordEncoder()
            .username(username)
            .password(password)
            .roles("USER")
            .build()
    return MapReactiveUserDetailsService(user)
}

val username = "user"
val password = "password"
fun repoAuthenticationManager(): UserDetailsRepositoryReactiveAuthenticationManager {
    return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService(username, password))
}

fun basicAuth() =
        Base64.getEncoder().encode("$username:$password".toByteArray())?.toString(Charset.defaultCharset())
