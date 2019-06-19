package io.pravega.flink.benchmark

import java.net.URI
import java.util.concurrent.TimeUnit

import io.pravega.client.netty.impl.ConnectionFactoryImpl
import io.pravega.client.stream.StreamConfiguration
import io.pravega.client.stream.impl.{ControllerImpl, ControllerImplConfig}
import io.pravega.connectors.flink.PravegaConfig
import org.slf4j.LoggerFactory

object PravegaUtil {
	
	val SCOPE_PARAM = "scope"
	val DEFAULT_SCOPE = "scope0"
	val STREAM_PARAM = "stream"
	val DEFAULT_STREAM = "stream0"
	val CONTROLLER_PARAM = "controller"
	val DEFAULT_CONTROLLER = "tcp://127.0.0.1:9090"
	
	private val LOG = LoggerFactory getLogger PravegaUtil.getClass.getSimpleName
	
	def config(scope: String, controllerUri: URI): PravegaConfig =
		PravegaConfig
			.fromDefaults()
			.withDefaultScope(scope)
			.withControllerURI(controllerUri)
	
	def prepare(scope: String, stream: String, pravegaConfig: PravegaConfig): Unit = {
		val controllerConfig = ControllerImplConfig
			.builder
			.clientConfig(pravegaConfig.getClientConfig)
			.build
		val connectionFactory = new ConnectionFactoryImpl(pravegaConfig.getClientConfig)
		val controller = new ControllerImpl(controllerConfig, connectionFactory.getInternalExecutor)
		try {
			controller
				.createScope(scope)
				.get(100, TimeUnit.SECONDS)
		} catch {
			case e: Throwable => LOG.warn("Scope creating failure: " + e)
		}
		try {
			val streamConfig = StreamConfiguration.builder.build
			controller
				.createStream(scope, stream, streamConfig)
				.get(100, TimeUnit.SECONDS)
		} catch {
			case e: Throwable => LOG.warn("Stream creating failure: " + e)
		}
		if (controller != null) {
			try {
				controller.close()
			} catch {
				case e: Throwable => LOG.warn("Controller closing failure: " + e)
			}
		}
	}
}
