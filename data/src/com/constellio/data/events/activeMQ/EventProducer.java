package com.constellio.data.events.activeMQ;

public interface EventProducer extends Runnable {

	void close();
}
