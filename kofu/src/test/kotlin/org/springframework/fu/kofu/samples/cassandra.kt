package org.springframework.fu.kofu.samples

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.cassandra.cassandra
import org.springframework.fu.kofu.cassandra.reactiveCassandra
import java.time.Duration

fun cassandra() {
	application {
		cassandra {
			localDatacenter = "datacenter1"
			keyspaceName = "keyspaceOne"
			sessionName = "cluster"
			username = "user"
			password = "password"
			ssl = false
			contactPoints = listOf("localhost")
			compression = CassandraProperties.Compression.NONE
			consistencyLevel = DefaultConsistencyLevel.ALL as DefaultConsistencyLevel
			serialConsistencyLevel = DefaultConsistencyLevel.ALL as DefaultConsistencyLevel
			connectTimeout = Duration.ofSeconds(1)
			fetchSize = 5000
			readTimeout = Duration.ofSeconds(2)
		}
	}
}

fun reactiveCassandra() {
	application {
		reactiveCassandra {
			localDatacenter = "datacenter1"
			keyspaceName = "keyspaceOne"
			sessionName = "cluster"
			username = "user"
			password = "password"
			ssl = false
			contactPoints = listOf("localhost")
			compression = CassandraProperties.Compression.NONE
			consistencyLevel = DefaultConsistencyLevel.ANY as DefaultConsistencyLevel
			serialConsistencyLevel = DefaultConsistencyLevel.SERIAL as DefaultConsistencyLevel
			connectTimeout = Duration.ofSeconds(1)
			fetchSize = 5000
			readTimeout = Duration.ofSeconds(2)
		}
	}
}
