package io.pravega.flink.benchmark

import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation

class RawBytesDeserializationSchema
extends DeserializationSchema[Array[Byte]] {

	override def deserialize(message: Array[Byte]): Array[Byte] =
		message

	override def isEndOfStream(nextElement: Array[Byte]): Boolean =
		false

	override def getProducedType: TypeInformation[Array[Byte]] =
		TypeInformation of classOf[Array[Byte]]
}
