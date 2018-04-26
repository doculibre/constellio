package com.constellio.data.events;

public class StandaloneEventBusSendingService extends EventBusSendingService {
	@Override
	public void sendRemotely(Event event) {
		//Does nothing
	}
}
