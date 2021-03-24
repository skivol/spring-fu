package org.springframework.fu.kofu.scheduling

import org.springframework.context.support.GenericApplicationContext
import org.springframework.fu.kofu.ConfigurationDsl
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.SchedulingInitializer

/**
 * Kofu DSL for spring-scheduling.
 */
class SchedulingDsl(private val init: SchedulingDsl.() -> Unit) : ConfigurationDsl({}) {
	var taskScheduler: TaskScheduler? = null

	override fun initialize(context: GenericApplicationContext) {
		super.initialize(context)
		init()
		SchedulingInitializer().initialize(context)
		taskScheduler?.let { scheduler -> beans { bean { scheduler } } }
	}
}

/**
 * Configure spring scheduling
 * @sample org.springframework.fu.kofu.samples.schedulingDsl
 */
fun ConfigurationDsl.scheduling(dsl: SchedulingDsl.() -> Unit = {}) {
	SchedulingDsl(dsl).initialize(context)
}
