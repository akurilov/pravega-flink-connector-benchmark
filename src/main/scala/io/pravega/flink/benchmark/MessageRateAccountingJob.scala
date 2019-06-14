package io.pravega.flink.benchmark

import java.net.URI
import java.util.concurrent.TimeUnit

import io.pravega.client.netty.impl.ConnectionFactoryImpl
import io.pravega.client.stream.{Stream, StreamConfiguration}
import io.pravega.client.stream.impl.{ControllerImpl, ControllerImplConfig}
import io.pravega.connectors.flink.{FlinkPravegaReader, PravegaConfig}
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.{Time => WindowingTime}
import org.slf4j.LoggerFactory

object MessageRateAccountingJob {

	private val JOB_NAME = "MessageRateAccountingJob"
	private val LOG = LoggerFactory getLogger JOB_NAME
	private val SCOPE_PARAM = "scope"
	private val DEFAULT_SCOPE = "scope0"
	private val STREAM_PARAM = "stream"
	private val DEFAULT_STREAM = "stream0"
	private val CONTROLLER_PARAM = "controller"
	private val DEFAULT_CONTROLLER = "tcp://127.0.0.1:9090"
	private val READ_TIMEOUT_MILLIS_PARAM = "readTimeoutMillis"
	private val DEFAULT_READ_TIMEOUT_MILLIS = Long.MaxValue

	@throws[Exception]
	def main(args: Array[String]) {
		LOG info("Starting the " + JOB_NAME + " with args: " + (args mkString ", "))
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool fromArgs args
		val scope = params.get(SCOPE_PARAM, DEFAULT_SCOPE)
		val stream = params.get(STREAM_PARAM, DEFAULT_STREAM)
		val controllerUri = new URI(params.get(CONTROLLER_PARAM, DEFAULT_CONTROLLER))
		val readTimeoutMillis = Time milliseconds params.getLong(READ_TIMEOUT_MILLIS_PARAM, DEFAULT_READ_TIMEOUT_MILLIS)
		val pravegaConfig = PravegaConfig
			.fromParams(params)
			.withDefaultScope(scope)
    		.withControllerURI(controllerUri)
		// create the Pravega input stream
		val controllerConfig = ControllerImplConfig
			.builder
			.clientConfig(pravegaConfig.getClientConfig)
			.build
		val connectionFactory = new ConnectionFactoryImpl(pravegaConfig.getClientConfig)
		val controller = new ControllerImpl(controllerConfig, connectionFactory.getInternalExecutor)
		try {
			controller
				.createScope(scope)
				.get(10, TimeUnit.SECONDS)
			val streamConfig = StreamConfiguration.builder.build
			controller
				.createStream(scope, stream, streamConfig)
				.get(10, TimeUnit.SECONDS)
		} finally {
			if (controller != null) {
				controller.close()
			}
		}
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		// create the Pravega source to read a stream of text
		val source = FlinkPravegaReader
			.builder[Array[Byte]]
			.withPravegaConfig(pravegaConfig)
			.withDeserializationSchema(new RawBytesDeserializationSchema)
    		.withEventReadTimeout(readTimeoutMillis)
			.forStream(Stream.of(scope, stream))
			.build
		// count each word over a 1 second time period
		val dataStream = env
			.addSource(source)
			.map((x: Array[Byte]) => 1L)
			.timeWindowAll(WindowingTime.seconds(1))
			.reduce(java.lang.Long.sum)
		dataStream.print
		env execute JOB_NAME
	}
}
