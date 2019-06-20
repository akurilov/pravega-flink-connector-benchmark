package io.pravega.flink.benchmark.event

import java.util.concurrent.locks.LockSupport

import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}

class EventPayloadGeneratorSource(size: Int, rate: Double)
extends RichParallelSourceFunction[Array[Byte]] {

	val delayNanos = Long(
		if(rate > 0) {
			1e9 / rate
		} else {
			0
		}
	)

	var cancelled = false
	
	override def run(ctx: SourceFunction.SourceContext[Array[Byte]]): Unit = {
		var nextTimeNanos = 0L
		while(!cancelled) {
			if(nextTimeNanos < System.nanoTime()) {
				LockSupport.parkNanos(1)
			} else {
				nextTimeNanos = System.nanoTime() + delayNanos
				ctx.collect(new Array[Byte](size))
			}
		}
	}
	
	override def cancel(): Unit = {
		cancelled = true
	}
}
