package com.constellio.data.events;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class SolrEventBusSendingServiceAcceptanceTest extends ConstellioTest {

	EventBus localEventBus1;
	EventBus localEventBus2;
	EventBus remoteEventBus1;
	EventBus remoteEventBus2;

	List<Event> localEventBus1ReceivedEvents = new ArrayList<>();
	List<Event> localEventBus2ReceivedEvents = new ArrayList<>();
	List<Event> remoteEventBus1ReceivedEvents = new ArrayList<>();
	List<Event> remoteEventBus2ReceivedEvents = new ArrayList<>();

	SolrEventBusSendingService localSendingService;
	SolrEventBusSendingService remoteSendingService;

	@Before
	public void setup() {

		prepareSystem(withZeCollection());
		EventBusManager localEventBusManager = getDataLayerFactory().getEventBusManager();
		EventBusManager remoteEventBusManager = getDataLayerFactory("other-instance").getEventBusManager();

		localEventBus1 = localEventBusManager.createEventBus("bus1", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		localEventBus2 = localEventBusManager.createEventBus("bus2", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		remoteEventBus1 = remoteEventBusManager.createEventBus("bus1", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		remoteEventBus2 = remoteEventBusManager.createEventBus("bus2", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		SolrClient solrClient = getDataLayerFactory().getSolrServers().getSolrServer("notifications").getNestedSolrServer();

		localSendingService = new SolrEventBusSendingService(solrClient).setPollAndRetrieveFrequency(Duration.millis(1000));
		remoteSendingService = new SolrEventBusSendingService(solrClient).setPollAndRetrieveFrequency(Duration.millis(1000));

		//		SDKEventBusSendingService localSendingService = new SDKEventBusSendingService();
		//		SDKEventBusSendingService remoteSendingService = new SDKEventBusSendingService();

		localEventBusManager.setEventBusSendingService(localSendingService);
		remoteEventBusManager.setEventBusSendingService(remoteSendingService);
		//TestUtils.linkEventBus(getDataLayerFactory(), getDataLayerFactory("other-instance"));
		localEventBus1.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (localEventBus1ReceivedEvents) {
					localEventBus1ReceivedEvents.add(event);
				}
			}
		});

		localEventBus2.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (localEventBus2ReceivedEvents) {
					localEventBus2ReceivedEvents.add(event);
				}
			}
		});
		remoteEventBus1.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (remoteEventBus1ReceivedEvents) {
					remoteEventBus1ReceivedEvents.add(event);
				}
			}
		});

		remoteEventBus2.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (remoteEventBus2ReceivedEvents) {
					remoteEventBus2ReceivedEvents.add(event);
				}
			}
		});
	}

	@Test
	public void whenBothServersAreSendingEventsThenAllReceived()
			throws Exception {

		Thread remoteThread = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 2000000; i++) {
					remoteEventBus1.send("anEvent", i);
					remoteEventBus2.send("anEvent", i);
				}
			}
		};
		remoteThread.start();

		for (int i = 0; i < 1000000; i++) {
			localEventBus1.send("anEvent", i);
			localEventBus2.send("anEvent", i);
		}
		remoteThread.join();

		while (!localSendingService.sendQueue.isEmpty() || !remoteSendingService.sendQueue.isEmpty()) {
			System.out.println(localSendingService.sendQueue.size() + remoteSendingService.sendQueue.size() + " not sent yet");
			Thread.sleep(500);
		}

		Thread.sleep(10000);

		assertThat(localEventBus1ReceivedEvents.size()).isEqualTo(3000000);
		assertThat(localEventBus2ReceivedEvents.size()).isEqualTo(3000000);
		assertThat(remoteEventBus1ReceivedEvents.size()).isEqualTo(3000000);
		assertThat(remoteEventBus2ReceivedEvents.size()).isEqualTo(3000000);
	}
}
