package io.pravega.flink.benchmark

import java.util.concurrent.TimeUnit

import io.pravega.client.netty.impl.ConnectionFactoryImpl
import io.pravega.client.stream.{Stream, StreamConfiguration}
import io.pravega.client.stream.impl.{ControllerImpl, ControllerImplConfig}
import io.pravega.connectors.flink.{FlinkPravegaReader, PravegaConfig}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.slf4j.LoggerFactory

object MessageRateAccountingJob {

	private val JOB_NAME = "MessageRateAccountingJob"
	private val LOG = LoggerFactory getLogger JOB_NAME
	private val DEFAULT_SCOPE = "scope1"
	private val DEFAULT_STREAM = "stream2"
	private val STREAM_PARAM = "stream"

	@throws[Exception]
	def main(args: Array[String]) {
		LOG info("Starting the " + JOB_NAME + " ...")
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool fromArgs args
		val pravegaConfig = PravegaConfig
			.fromParams(params)
			.withDefaultScope(DEFAULT_SCOPE)
		// create the Pravega input stream
		val controllerConfig = ControllerImplConfig
			.builder
			.clientConfig(pravegaConfig.getClientConfig)
			.build
		val connectionFactory = new ConnectionFactoryImpl(pravegaConfig.getClientConfig)
		val controller = new ControllerImpl(controllerConfig, connectionFactory.getInternalExecutor)
		try {
			controller
				.createScope(DEFAULT_SCOPE)
				.get(10, TimeUnit.SECONDS)
			val streamConfig = StreamConfiguration.builder.build
			controller
				.createStream(DEFAULT_SCOPE, DEFAULT_STREAM, streamConfig)
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
			.forStream(Stream.of(DEFAULT_SCOPE, DEFAULT_STREAM))
			.build
		// count each word over a 1 second time period
		val dataStream = env
			.addSource(source)
			.map((x: Array[Byte]) => 1L)
			.timeWindowAll(Time.seconds(1))
			.reduce(java.lang.Long.sum)
		dataStream.print
		env execute JOB_NAME
	}
}
