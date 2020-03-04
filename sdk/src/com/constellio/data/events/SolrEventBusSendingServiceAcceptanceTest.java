package com.constellio.data.events;

import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_DataIsNotSerializable;
import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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

	AtomicInteger localSentEventExtensionCalledCounter = new AtomicInteger();
	AtomicInteger remoteSentEventExtensionCalledCounter = new AtomicInteger();

	AtomicInteger localReceivedEventExtensionCalledCounter = new AtomicInteger();
	AtomicInteger remoteReceivedEventExtensionCalledCounter = new AtomicInteger();

	@Before
	public void setup() {

		prepareSystem(withZeCollection());

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSolrEventBusRetrieveAndSendFrequency(Duration.millis(100));
			}
		});

		EventBusManager localEventBusManager = getDataLayerFactory().getEventBusManager();
		EventBusManager remoteEventBusManager = getDataLayerFactory("other-instance").getEventBusManager();

		localEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToSerialize());
		localEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToDeserialize());
		remoteEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToSerialize());
		remoteEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToDeserialize());

		localEventBus1 = localEventBusManager.createEventBus("bus1", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		localEventBus2 = localEventBusManager.createEventBus("bus2", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		remoteEventBus1 = remoteEventBusManager.createEventBus("bus1", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		remoteEventBus2 = remoteEventBusManager.createEventBus("bus2", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		SolrClient solrClient = getDataLayerFactory().getSolrServers().getSolrServer("notifications").getNestedSolrServer();

		localSendingService = new SolrEventBusSendingService(solrClient).setPollAndRetrieveFrequency(Duration.millis(1000));
		remoteSendingService = new SolrEventBusSendingService(solrClient).setPollAndRetrieveFrequency(Duration.millis(1000));

		localEventBusManager.setEventBusSendingService(localSendingService);
		remoteEventBusManager.setEventBusSendingService(remoteSendingService);
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

		getDataLayerFactory().getExtensions().getSystemWideExtensions().eventBusManagerExtensions
				.add(new EventBusManagerExtension() {
					@Override
					public void onEventReceived(ReceivedEventParams params) {
						if (params.isRemoteEvent()) {
							localReceivedEventExtensionCalledCounter.incrementAndGet();
						}
					}

					@Override
					public void onEventSent(SentEventParams params) {
						localSentEventExtensionCalledCounter.incrementAndGet();
					}
				});

		getDataLayerFactory("other-instance").getExtensions().getSystemWideExtensions().eventBusManagerExtensions
				.add(new EventBusManagerExtension() {
					@Override
					public void onEventReceived(ReceivedEventParams params) {
						if (params.isRemoteEvent()) {
							remoteReceivedEventExtensionCalledCounter.incrementAndGet();
						}
					}

					@Override
					public void onEventSent(SentEventParams params) {
						remoteSentEventExtensionCalledCounter.incrementAndGet();
					}
				});
	}

	@Test
	public void whenBothServersAreSendingEventsThenAllReceivedAndExtensionsCalled()
			throws Exception {

		Thread remoteThread = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 20000; i++) {
					remoteEventBus1.send("anEvent", i);
					remoteEventBus2.send("anEvent", i);
				}
			}
		};
		remoteThread.start();

		for (int i = 0; i < 10000; i++) {
			localEventBus1.send("anEvent", i);
			localEventBus2.send("anEvent", i);
		}
		remoteThread.join();

		while (remoteReceivedEventExtensionCalledCounter.get() < 10000
			   || localReceivedEventExtensionCalledCounter.get() < 30000) {
			System.out.println(remoteReceivedEventExtensionCalledCounter.get());
			System.out.println(localReceivedEventExtensionCalledCounter.get());
			Thread.sleep(50);
		}

		Thread.sleep(1000);

		assertThat(localEventBus1ReceivedEvents.size()).isEqualTo(30000);
		assertThat(localEventBus2ReceivedEvents.size()).isEqualTo(30000);
		assertThat(remoteEventBus1ReceivedEvents.size()).isEqualTo(30000);
		assertThat(remoteEventBus2ReceivedEvents.size()).isEqualTo(30000);

		assertThat(countDifferentIds(localEventBus1ReceivedEvents)).isEqualTo(30000);
		assertThat(countDifferentIds(localEventBus2ReceivedEvents)).isEqualTo(30000);
		assertThat(countDifferentIds(remoteEventBus1ReceivedEvents)).isEqualTo(30000);
		assertThat(countDifferentIds(remoteEventBus2ReceivedEvents)).isEqualTo(30000);

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(30000);
		assertThat(countSolrDocumentWithBus("bus2")).isEqualTo(30000);

		givenTimeIs(TimeProvider.getLocalDateTime().plusMinutes(4));

		remoteEventBus1.send("anEvent1");
		localEventBus1.send("anEvent1");
		remoteEventBus2.send("anEvent2");
		localEventBus2.send("anEvent2");

		Thread.sleep(3500);

		localSendingService.deleteOldEvents();
		localSendingService.commit();
		Thread.sleep(750);

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(30002);
		assertThat(countSolrDocumentWithBus("bus2")).isEqualTo(30002);

		givenTimeIs(TimeProvider.getLocalDateTime().plusMinutes(1));
		localEventBus2.send("anEvent2");
		Thread.sleep(3500);
		localSendingService.deleteOldEvents();
		localSendingService.commit();
		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(2);
		assertThat(countSolrDocumentWithBus("bus2")).isEqualTo(3);
	}

	@Test
	public void whenSendingAnEventWhichFailToDeserializeThenSentAndNotReceivedButEventBusStillWorkingOnbothSides()
			throws Exception {
		remoteEventBus1.send("anEvent");
		remoteEventBus1.send("anEvent2");
		localEventBus1.send("anEvent3");
		localEventBus1.send("anEvent4");

		while (remoteReceivedEventExtensionCalledCounter.get() < 2
			   || localReceivedEventExtensionCalledCounter.get() < 2) {
			Thread.sleep(50);
		}

		remoteEventBus1.send("anInvalidEvent", asList(new TestClassWhichIsNeverDeserializable()));
		remoteEventBus1.send("anEvent5");

		Thread.sleep(4000);

		remoteEventBus1.send("anEvent6");

		while (remoteReceivedEventExtensionCalledCounter.get() < 2
			   || localReceivedEventExtensionCalledCounter.get() < 4) {
			Thread.sleep(50);
		}

		assertThat(localEventBus1ReceivedEvents).extracting("type")
				.containsOnly("anEvent", "anEvent2", "anEvent3", "anEvent4", "anEvent5", "anEvent6");
		assertThat(remoteEventBus1ReceivedEvents).extracting("type")
				.containsOnly("anEvent", "anEvent2", "anEvent3", "anEvent4", "anEvent5", "anEvent6", "anInvalidEvent");

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(6);
	}

	@Test
	public void whenSendingAnEventWhichFailToSerializeThenSentAndNotReceivedButEventBusStillWorkingOnbothSides()
			throws Exception {
		remoteEventBus1.send("anEvent");
		remoteEventBus1.send("anEvent2");
		localEventBus1.send("anEvent3");
		localEventBus1.send("anEvent4");

		while (remoteReceivedEventExtensionCalledCounter.get() < 2
			   || localReceivedEventExtensionCalledCounter.get() < 2) {
			Thread.sleep(50);
		}

		try {
			remoteEventBus1.send("anInvalidEvent", asList(new TestClassWhichIsNeverSerializable()));
			fail("Exception expected");

		} catch (EventBusManagerRuntimeException_DataIsNotSerializable e) {
			//OK
		}
		remoteEventBus1.send("anEvent5");

		Thread.sleep(4000);

		remoteEventBus1.send("anEvent6");

		while (remoteReceivedEventExtensionCalledCounter.get() < 2
			   || localReceivedEventExtensionCalledCounter.get() < 4) {
			Thread.sleep(50);
		}

		assertThat(localEventBus1ReceivedEvents).extracting("type")
				.containsOnly("anEvent", "anEvent2", "anEvent3", "anEvent4", "anEvent5", "anEvent6");
		assertThat(remoteEventBus1ReceivedEvents).extracting("type")
				.containsOnly("anEvent", "anEvent2", "anEvent3", "anEvent4", "anEvent5", "anEvent6", "anInvalidEvent");

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(6);
	}

	@Test
	public void givenEventsAlreadyExistingWhenTheSolrSendingServiceIsStartedThenMarkedAsReceived()
			throws Exception {

		remoteEventBus1.send("anEvent");
		remoteEventBus2.send("anEvent2");
		localEventBus1.send("anEvent3");
		localEventBus2.send("anEvent4");
		Thread.sleep(4000);

		final List<Event> thirdInstanceBus1ReceivedEvents = new ArrayList<>();
		final List<Event> thirdInstanceBus2ReceivedEvents = new ArrayList<>();
		final AtomicInteger thirdInstanceReceivedEventExtensionCalledCounter = new AtomicInteger();
		final AtomicInteger thirdInstanceSendEventExtensionCalledCounter = new AtomicInteger();

		EventBusManager thirdInstanceEventBusManager = setupPausedThirdInstance(
				thirdInstanceBus1ReceivedEvents, thirdInstanceBus2ReceivedEvents,
				thirdInstanceReceivedEventExtensionCalledCounter, thirdInstanceSendEventExtensionCalledCounter);

		remoteEventBus1.send("anEvent5");
		remoteEventBus2.send("anEvent6");
		localEventBus1.send("anEvent7");
		localEventBus2.send("anEvent8");
		localEventBus2.send("anEvent9");

		System.out.println("Waiting 4 seconds");
		Thread.sleep(4000);

		assertThat(thirdInstanceBus1ReceivedEvents).extracting("type").isEmpty();
		assertThat(thirdInstanceBus2ReceivedEvents).extracting("type").isEmpty();

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(4);
		assertThat(countSolrDocumentWithBus("bus2")).isEqualTo(5);

		thirdInstanceEventBusManager.resume();

		localEventBus1.send("anEvent10");
		remoteEventBus1.send("anEvent11");

		System.out.println("Waiting 4 seconds");
		Thread.sleep(4000);

		assertThat(thirdInstanceBus1ReceivedEvents).extracting("type")
				.containsOnly("anEvent5", "anEvent7", "anEvent10", "anEvent11");
		assertThat(thirdInstanceBus2ReceivedEvents).extracting("type").containsOnly("anEvent6", "anEvent8", "anEvent9");

		assertThat(countSolrDocumentWithBus("bus1")).isEqualTo(6);
		assertThat(countSolrDocumentWithBus("bus2")).isEqualTo(5);
	}

	protected EventBusManager setupPausedThirdInstance(final List<Event> bus1ReceivedEvents,
													   final List<Event> bus2ReceivedEvents,
													   final AtomicInteger localReceivedEventExtensionCalledCounter,
													   final AtomicInteger localSentEventExtensionCalledCounter) {
		EventBusManager thirdInstanceEventBusManager = getDataLayerFactory("third-instance").getEventBusManager();
		thirdInstanceEventBusManager.pause();
		thirdInstanceEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToSerialize());
		thirdInstanceEventBusManager.eventDataSerializer.register(new EventDataSerializerExtensionFailingToDeserialize());

		EventBus thirdInstanceEventBus1 = thirdInstanceEventBusManager
				.createEventBus("bus1", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		EventBus thirdInstanceEventBus2 = thirdInstanceEventBusManager
				.createEventBus("bus2", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		SolrClient solrClient = getDataLayerFactory().getSolrServers().getSolrServer("notifications").getNestedSolrServer();

		EventBusSendingService thirdInstanceSendingService = new SolrEventBusSendingService(solrClient)
				.setPollAndRetrieveFrequency(Duration.millis(1000));
		thirdInstanceEventBusManager.setEventBusSendingService(thirdInstanceSendingService);

		thirdInstanceEventBus1.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (bus1ReceivedEvents) {
					bus1ReceivedEvents.add(event);
				}
			}
		});

		thirdInstanceEventBus2.register(new EventBusListener() {
			@Override
			public void onEventReceived(Event event) {
				synchronized (bus2ReceivedEvents) {
					bus2ReceivedEvents.add(event);
				}
			}
		});

		getDataLayerFactory().getExtensions().getSystemWideExtensions().eventBusManagerExtensions
				.add(new EventBusManagerExtension() {
					@Override
					public void onEventReceived(ReceivedEventParams params) {
						if (params.isRemoteEvent()) {
							localReceivedEventExtensionCalledCounter.incrementAndGet();
						}
					}

					@Override
					public void onEventSent(SentEventParams params) {
						localSentEventExtensionCalledCounter.incrementAndGet();
					}
				});

		return thirdInstanceEventBusManager;
	}

	private static class TestClassWhichIsNeverDeserializable {

	}

	private static class TestClassWhichIsNeverSerializable {

	}

	private static class EventDataSerializerExtensionFailingToDeserialize implements EventDataSerializerExtension {

		@Override
		public String getId() {
			return "TestClassWhichIsNeverDeserializable";
		}

		@Override
		public Class<?> getSupportedDataClass() {
			return TestClassWhichIsNeverDeserializable.class;
		}

		@Override
		public String serialize(Object data) {
			return data.toString();
		}

		@Override
		public Object deserialize(String deserialize) {
			throw new RuntimeException("Oh bobo!");
		}
	}

	private static class EventDataSerializerExtensionFailingToSerialize implements EventDataSerializerExtension {

		@Override
		public String getId() {
			return "TestClassWhichIsNeverSerializable";
		}

		@Override
		public Class<?> getSupportedDataClass() {
			return TestClassWhichIsNeverSerializable.class;
		}

		@Override
		public String serialize(Object data) {
			throw new RuntimeException("Oh bobo!");
		}

		@Override
		public Object deserialize(String deserialize) {
			throw new AssertionError(
					"TestClassWhichIsNeverSerializable should never be deserialized, since it is not serializable");
		}
	}

	private int countSolrDocumentWithBus(String bus)
			throws IOException, SolrServerException {
		SolrClient client = getDataLayerFactory().getNotificationsVaultServer().getNestedSolrServer();

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("rows", "0");
		params.set("q", "bus_s:" + bus);

		QueryResponse response = client.query(params);
		return (int) response.getResults().getNumFound();
	}

	private int countDifferentIds(List<Event> events) {
		Set<String> ids = new HashSet<>();

		for (Event event : events) {
			ids.add(event.getId());
		}

		return ids.size();
	}
}
