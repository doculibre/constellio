package com.constellio.data.events;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;

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
				|| localReceivedEventExtensionCalledCounter.get() < 20000) {
			Thread.sleep(50);
		}

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
