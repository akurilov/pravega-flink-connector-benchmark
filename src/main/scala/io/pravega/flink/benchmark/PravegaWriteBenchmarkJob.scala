package io.pravega.flink.benchmark

import java.net.URI

import io.pravega.connectors.flink.{FlinkPravegaWriter, PravegaWriterMode}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.{Time => WindowingTime}
import io.pravega.flink.benchmark.PravegaUtil._
import org.slf4j.LoggerFactory

object PravegaWriteBenchmarkJob {
	
	private val JOB_NAME = PravegaReadBenchmarkJob.getClass.getSimpleName
	private val LOG = LoggerFactory getLogger JOB_NAME
	
	@throws[Exception]
	def main(args: Array[String]) {
		LOG info("Starting the " + JOB_NAME + " with args: " + (args mkString ", "))
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool fromArgs args
		val scope = params.get(SCOPE_PARAM, DEFAULT_SCOPE)
		val stream = params.get(STREAM_PARAM, DEFAULT_STREAM)
		val controllerUri = new URI(params.get(CONTROLLER_PARAM, DEFAULT_CONTROLLER))
		//
		val pravegaConfig = PravegaUtil config(scope, controllerUri)
		PravegaUtil prepare(scope, stream, pravegaConfig)
		//
		val env = StreamExecutionEnvironment.getExecutionEnvironment
		val src = new EventGeneratorSource
		val sink = FlinkPravegaWriter
    			.builder[Array[Byte]]
    			.withPravegaConfig(pravegaConfig)
    			.withSerializationSchema(new RawBytesSerializationSchema)
    			.withWriterMode(PravegaWriterMode.EXACTLY_ONCE)
    			.build()
		val dataStream = env
			.addSource(src)
    		.map((_: Array[Byte]) => 1L)
			.timeWindowAll(WindowingTime seconds 1)
			.reduce(java.lang.Long.sum)
		dataStream.addSink(sink)
		dataStream.print
		env execute JOB_NAME
	}
}
