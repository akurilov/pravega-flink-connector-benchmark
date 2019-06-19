package io.pravega.flink.benchmark

import org.apache.flink.api.common.serialization.SerializationSchema

class RawBytesSerializationSchema
extends SerializationSchema[Array[Byte]] {
	
	override def serialize(data: Array[Byte]): Array[Byte] =
		data
}
