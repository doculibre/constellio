package com.constellio.data.events;

import java.util.ArrayList;
import java.util.List;

public class SDKEventBusSendingService extends EventBusSendingService {

	List<EventBusSendingService> others = new ArrayList<>();

	@Override
	public void sendRemotely(Event event) {
		for (EventBusSendingService other : others) {
			other.eventReceiver.receive(event);
		}
	}

	public static void interconnect(SDKEventBusSendingService... services) {
		for (int i = 0; i < services.length; i++) {
			for (int j = 0; j < services.length; j++) {
				if (i != j) {
					services[i].others.add(services[j]);
				}
			}
		}
	}
}
