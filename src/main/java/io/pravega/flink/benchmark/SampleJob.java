package io.pravega.flink.benchmark;

import io.pravega.client.stream.EventRead;
import io.pravega.connectors.flink.FlinkPravegaReader;
import io.pravega.connectors.flink.PravegaConfig;
import io.pravega.connectors.flink.serialization.PravegaSerialization;
import lombok.val;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleJob {

	private static final Logger LOG = LoggerFactory.getLogger(SampleJob.class);
	private static final String DEFAULT_SCOPE = "scope0";
	private static final String DEFAULT_STREAM = "stream0";
	private static final String STREAM_PARAM = "stream";

	public static void main(final String... args)
	throws Exception {
		LOG.info("Starting the sample job...");
		// initialize the parameter utility tool in order to retrieve input parameters
		val params = ParameterTool.fromArgs(args);
		val pravegaConfig = PravegaConfig
			.fromParams(params)
			.withDefaultScope(DEFAULT_SCOPE);
		// create the Pravega input stream
		val stream = Utils.createStream(pravegaConfig, params.get(STREAM_PARAM, DEFAULT_STREAM));
		val env = StreamExecutionEnvironment.getExecutionEnvironment();
		// create the Pravega source to read a stream of text
		val source = FlinkPravegaReader.builder()
			.withPravegaConfig(pravegaConfig)
			.forStream(stream)
			.build();
		// count each word over a 10 second time period
		val dataStream = env
			.addSource(source)
			.name("Pravega Stream")
			.timeWindowAll(Time.seconds(10))
			.process(new ProcessAllWindowFunction<Object, Object, TimeWindow>() {
				@Override
				public void process(
					final Context context, final Iterable<Object> elements, final Collector<Object> out
				) throws Exception {
				}
			});
	}
}
