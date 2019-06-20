package io.pravega.flink.benchmark.event

import io.pravega.connectors.flink.PravegaEventRouter

class ConstantEventRouter(routingKey: String)
extends PravegaEventRouter[Array[Byte]] {

  override def getRoutingKey(t: Array[Byte]): String =
    routingKey
}
