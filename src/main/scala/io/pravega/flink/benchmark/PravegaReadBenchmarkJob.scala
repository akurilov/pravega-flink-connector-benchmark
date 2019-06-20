package io.pravega.flink.benchmark

import java.net.URI

import io.pravega.client.stream.Stream
import io.pravega.connectors.flink.FlinkPravegaReader
import io.pravega.flink.benchmark.Constants._
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.{Time => WindowingTime}
import org.slf4j.LoggerFactory
import serde.RawBytesDeserializationSchema

object PravegaReadBenchmarkJob {

	private val JOB_NAME = PravegaReadBenchmarkJob.getClass.getSimpleName
	private val LOG = LoggerFactory getLogger JOB_NAME
	private val READ_TIMEOUT_MILLIS_PARAM = "readTimeoutMillis"
	private val DEFAULT_READ_TIMEOUT_MILLIS = Long.MaxValue

	@throws[Exception]
	def main(args: Array[String]): Unit = {
		LOG info("Starting the " + JOB_NAME + " with args: " + (args mkString ", "))
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool fromArgs args
		val scope = params get(SCOPE_PARAM, DEFAULT_SCOPE)
		val stream = params get(STREAM_PARAM, DEFAULT_STREAM)
		val controllerUri = new URI(params get(CONTROLLER_PARAM, DEFAULT_CONTROLLER))
		val readTimeoutMillis = Time milliseconds params.getLong(READ_TIMEOUT_MILLIS_PARAM, DEFAULT_READ_TIMEOUT_MILLIS)
		//
		val pravegaConfig = PravegaUtil config(scope, controllerUri)
		PravegaUtil prepare(scope, stream, pravegaConfig)
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		// create the Pravega source to read a stream of text
		val src = FlinkPravegaReader
			.builder[Array[Byte]]
			.withPravegaConfig(pravegaConfig)
			.withDeserializationSchema(new RawBytesDeserializationSchema)
			.withEventReadTimeout(readTimeoutMillis)
			.forStream(Stream of(scope, stream))
			.build
		// count each message over a 1 second time period
		val dataStream = env
			.addSource(src)
			.map((_: Array[Byte]) => 1L)
			.timeWindowAll(WindowingTime seconds 1)
			.reduce(java.lang.Long.sum)
		dataStream.print
		env execute JOB_NAME
	}
}
