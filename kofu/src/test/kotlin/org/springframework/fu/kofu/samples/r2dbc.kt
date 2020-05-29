package org.springframework.fu.kofu.samples

import org.springframework.boot.WebApplicationType
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.r2dbc.r2dbcH2
import org.springframework.fu.kofu.r2dbc.r2dbcMssql
import org.springframework.fu.kofu.r2dbc.r2dbcMysql
import org.springframework.fu.kofu.r2dbc.r2dbcPostgresql
import java.time.Duration
import java.time.temporal.ChronoUnit

fun r2dbcPostgresql() {
	application(WebApplicationType.NONE) {
		r2dbcPostgresql {
			host = "dbserver"
			port = 1234
			connectTimeout = Duration.of(10, ChronoUnit.SECONDS)
		}
	}
}

fun r2dbcH2() {
	application(WebApplicationType.NONE) {
		r2dbcH2()
	}
}


fun r2dbcMssql() {
	application(WebApplicationType.NONE) {
		r2dbcMssql {
			host = "dbserver"
			port = 1234
		}
	}
}


fun r2dbcMysql() {
	application(WebApplicationType.NONE) {
		r2dbcMysql("localhost")
	}
}
