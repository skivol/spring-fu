package org.springframework.fu.kofu.samples

import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.scheduling.scheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import java.util.concurrent.Executors

class Task {
	@Scheduled(fixedRate = 5_000)
	fun run() {
		println("Task execution")
	}
}
fun schedulingDsl() {
	reactiveWebApplication {
		beans {
			bean<Task>()
		}
		scheduling {
			taskScheduler = ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor())
		}
	}
}
