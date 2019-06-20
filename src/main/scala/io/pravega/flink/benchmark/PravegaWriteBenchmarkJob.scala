package io.pravega.flink.benchmark

import java.net.URI

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import io.pravega.connectors.flink.{FlinkPravegaWriter, PravegaWriterMode}
import io.pravega.flink.benchmark.Constants._
import io.pravega.flink.benchmark.event.{ConstantEventRouter, EventPayloadGeneratorSource}
import org.slf4j.LoggerFactory
import serde.RawBytesSerializationSchema

object PravegaWriteBenchmarkJob {
	
	private val JOB_NAME = PravegaReadBenchmarkJob.getClass.getSimpleName
	private val LOG = LoggerFactory getLogger JOB_NAME
	private val EVT_PAYLOAD_SIZE_PARAM = "evtPayloadSize"
	private val DEFAULT_EVT_PAYLOAD_SIZE = 1
	private val RATE_LIMIT_PARAM = "rateLimit"
	private val DEFAULT_RATE_LIMIT = 1.0
	
	@throws[Exception]
	def main(args: Array[String]) {
		LOG info("Starting the " + JOB_NAME + " with args: " + (args mkString ", "))
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool fromArgs args
		val scope = params.get(SCOPE_PARAM, DEFAULT_SCOPE)
		val stream = params.get(STREAM_PARAM, DEFAULT_STREAM)
		val controllerUri = new URI(params.get(CONTROLLER_PARAM, DEFAULT_CONTROLLER))
		val evtSize = params.getInt(EVT_PAYLOAD_SIZE_PARAM, DEFAULT_EVT_PAYLOAD_SIZE)
		val rateLimit = params.getDouble(RATE_LIMIT_PARAM, DEFAULT_RATE_LIMIT)
		//
		val pravegaConfig = PravegaUtil config(scope, controllerUri)
		PravegaUtil prepare(scope, stream, pravegaConfig)
		//
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val source = new EventPayloadGeneratorSource(evtSize, rateLimit)
		val dataStream = env addSource source
		val sink = FlinkPravegaWriter
			.builder[Array[Byte]]
			.withPravegaConfig(pravegaConfig)
  		.withEventRouter(new ConstantEventRouter(JOB_NAME))
  		.withSerializationSchema(new RawBytesSerializationSchema)
			.withWriterMode(PravegaWriterMode.EXACTLY_ONCE)
			.build()
		dataStream addSink sink
		env execute JOB_NAME
	}
}
