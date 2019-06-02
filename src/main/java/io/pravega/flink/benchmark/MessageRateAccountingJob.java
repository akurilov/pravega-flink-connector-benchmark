package io.pravega.flink.benchmark;

import io.pravega.connectors.flink.FlinkPravegaReader;
import io.pravega.connectors.flink.PravegaConfig;
import io.pravega.connectors.flink.serialization.PravegaSerialization;
import lombok.val;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRateAccountingJob {

	private static final Logger LOG = LoggerFactory.getLogger(MessageRateAccountingJob.class);
	private static final String DEFAULT_SCOPE = "scope0";
	private static final String DEFAULT_STREAM = "stream0";
	private static final String STREAM_PARAM = "stream";
	private static final String JOB_NAME = MessageRateAccountingJob.class.getSimpleName();

	public static void main(final String... args)
	throws Exception {
		LOG.info("Starting the " + JOB_NAME + " ...");
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool.fromArgs(args);
		val pravegaConfig = PravegaConfig
			.fromParams(params)
			.withDefaultScope(DEFAULT_SCOPE);
		// create the Pravega input stream
		val stream = Utils.createStream(pravegaConfig, params.get(STREAM_PARAM, DEFAULT_STREAM));
		val env = StreamExecutionEnvironment.getExecutionEnvironment();
		// create the Pravega source to read a stream of text
		val source = FlinkPravegaReader.<byte[]>builder()
			.withPravegaConfig(pravegaConfig)
			.withDeserializationSchema(PravegaSerialization.deserializationFor(byte[].class))
			.forStream(stream)
			.build();
		// count each word over a 1 second time period
		val dataStream = env
			.addSource(source)
			.map(x -> 1L)
			.timeWindowAll(Time.seconds(1))
			.reduce(Long::sum);
		dataStream.print();
		env.execute(MessageRateAccountingJob.class.getSimpleName());
	}
}
