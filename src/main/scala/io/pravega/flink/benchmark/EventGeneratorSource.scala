package io.pravega.flink.benchmark

import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}

class EventGeneratorSource
extends RichParallelSourceFunction[Array[Byte]] {
	
	
	
	override def run(ctx: SourceFunction.SourceContext[Array[Byte]]): Unit = {
		// TODO
	}
	
	override def cancel(): Unit = {
		// TODO
	}
}
