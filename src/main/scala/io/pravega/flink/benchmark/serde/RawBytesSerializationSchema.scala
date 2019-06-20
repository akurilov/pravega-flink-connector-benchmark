package io.pravega.flink.benchmark.serde

import org.apache.flink.api.common.serialization.SerializationSchema

class RawBytesSerializationSchema
extends SerializationSchema[Array[Byte]] {
	
	override def serialize(data: Array[Byte]): Array[Byte] =
		data
}
