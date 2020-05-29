package com.sample

import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.webflux.webFlux
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok

val app = reactiveWebApplication {
	beans {
		bean<SampleService>()
		bean<SampleHandler>()
	}
	webFlux {
		port = if (profiles.contains("test")) 8181 else 8080
		router {
			val handler = ref<SampleHandler>()
			GET("/", handler::hello)
			GET("/api", handler::json)
		}
		codecs {
			string()
			jackson()
		}
	}
}

data class Sample(val message: String)

class SampleService {
	fun generateMessage() = "Hello world!"
}

@Suppress("UNUSED_PARAMETER")
class SampleHandler(private val sampleService: SampleService) {
	fun hello(request: ServerRequest)= ok().bodyValue(sampleService.generateMessage())
	fun json(request: ServerRequest) = ok().bodyValue(Sample(sampleService.generateMessage()))
}

fun main() {
	app.run()
}
