package com.constellio.data.events;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_EventBusAlreadyExist;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_NoSuchEventBus;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class EventBusManagerTest extends ConstellioTest {

	@Mock EventBusSendingService sendingService;
	EventBusManager eventBusManager;

	@Mock EventBusListener listener1, listener2, listener3, listener4;

	@Mock DataLayerSystemExtensions systemExtensions;

	@Before
	public void setUp()
			throws Exception {
		eventBusManager = new EventBusManager(sendingService, systemExtensions);
	}

	@Test
	public void validatingAddRemoveEventBusOperations()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		EventBus schoolBus = eventBusManager.createEventBus("Bus d'école", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		assertThat(eventBusManager.hasEventBus("Bus magique")).isTrue();
		assertThat(eventBusManager.hasEventBus("Bus d'école")).isTrue();
		assertThat(eventBusManager.hasEventBus("Ourléhein Express")).isFalse();

		assertThat(eventBusManager.getEventBus("Bus magique")).isSameAs(magicBus);

		try {
			assertThat(eventBusManager.getEventBus("Ourléhein Express")).isSameAs(magicBus);
			fail("Exception expected");
		} catch (EventBusManagerRuntimeException_NoSuchEventBus e) {

		}

		try {
			eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
			fail("Exception expected");
		} catch (EventBusManagerRuntimeException_EventBusAlreadyExist e) {

		}

		eventBusManager.removeEventBus("Bus magique");
		assertThat(eventBusManager.hasEventBus("Bus magique")).isFalse();

		EventBus newMagicBus = eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		assertThat(newMagicBus).isNotSameAs(magicBus);

	}

	@Test
	public void givenMultipleEventBusReceivedEventsOrDispatchedToTheGoodOneAndEventWithUnknownBusAreDismissed()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		magicBus.register(listener1);
		magicBus.register(listener2);
		magicBus.register(listener4);
		magicBus.unregister(listener4);

		EventBus schoolBus = eventBusManager.createEventBus("Bus d'école", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		EventBus ourleheinExpressBus = eventBusManager.createEventBus("Ourléhein Express", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		ourleheinExpressBus.register(listener3);

		Event event1 = new Event("Bus magique", "flying", "1", 1l, "zeValue");
		Event event2 = new Event("Bus magique", "landing", "2", 2l, null);
		Event event3 = new Event("Bus d'école", "bringingKidsToSchool", "3", 3l, asList("Alice", "Bob"));
		Event event4 = new Event("Ourléhein Express", "driving", "4", 4l, asList("Quebec", "Matane"));
		Event event5 = new Event("ZeBus", "unknown", "5", 5l, "noData");

		eventBusManager.receive(event1);
		eventBusManager.receive(event2);
		eventBusManager.receive(event3);
		eventBusManager.receive(event4);
		eventBusManager.receive(event5);

		InOrder inOrder = Mockito.inOrder(listener1, listener2, listener3, listener4);
		inOrder.verify(listener1).onEventReceived(event1);
		inOrder.verify(listener2).onEventReceived(event1);
		inOrder.verify(listener1).onEventReceived(event2);
		inOrder.verify(listener2).onEventReceived(event2);
		inOrder.verify(listener3).onEventReceived(event4);

	}

	@Test
	public void whenSendingEventToPausedEventBusThenValidatedThenSentLocallyThenSentRemotely()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		magicBus.register(listener1);
		magicBus.register(listener2);

		eventBusManager = spy(eventBusManager);
		eventBusManager.eventDataSerializer = spy(eventBusManager.eventDataSerializer);
		Event event1 = new Event("Bus magique", "flying", "1", 1l, "zeValue");
		eventBusManager.send(event1, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		Event event2 = new Event("Bus magique", "landing", "1", 1l, "anotherValue");
		eventBusManager.send(event2, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		verify(eventBusManager, never()).receive(any(Event.class));
		verify(eventBusManager, never()).receive(any(Event.class), anyBoolean());
		verifyZeroInteractions(eventBusManager.eventDataSerializer);
		verify(eventBusManager.eventBusSendingService, never()).sendRemotely(any(Event.class));
	}


	@Test
	public void whenSendingEventToResumedEventBusThenValidatedThenSentLocallyThenSentRemotely()
			throws Exception {

		eventBusManager.resume();

		EventBus magicBus = eventBusManager.createEventBus("Bus magique", EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
		magicBus.register(listener1);
		magicBus.register(listener2);

		eventBusManager = spy(eventBusManager);
		eventBusManager.eventDataSerializer = spy(eventBusManager.eventDataSerializer);
		Event event1 = new Event("Bus magique", "flying", "1", 1l, "zeValue");
		eventBusManager.send(event1, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		Event event2 = new Event("Bus magique", "landing", "1", 1l, "anotherValue");
		eventBusManager.send(event2, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);

		InOrder inOrder = Mockito
				.inOrder(listener1, listener2, eventBusManager.eventDataSerializer, eventBusManager, sendingService);
		inOrder.verify(eventBusManager.eventDataSerializer).validateData("zeValue");
		inOrder.verify(eventBusManager).receive(event1, false);
		inOrder.verify(sendingService).sendRemotely(event1);
		inOrder.verify(eventBusManager.eventDataSerializer).validateData("anotherValue");
		inOrder.verify(eventBusManager).receive(event2, false);
		inOrder.verify(sendingService).sendRemotely(event2);

	}

	@Test
	public void whenSendingEventWithAtomicDataThenSerializedCorrectly()
			throws Exception {

		assertThatDataIsSerializable("test");
		assertThatDataIsSerializable(null);
		assertThatDataIsSerializable(1);
		assertThatDataIsSerializable(true);
		assertThatDataIsNotSerializable(eventBusManager);
		assertThatDataIsNotSerializable(new Thread());
		assertThatDataIsNotSerializable(new Thread());
		assertThatDataIsNotSerializable(new UnserializableObject("test"));

		eventBusManager.eventDataSerializer.register(anExtension);
		assertThatDataIsSerializable(new UnserializableObject("test"));
		assertThatDataIsSerializable(new UnserializableObject("~test2"));
		assertThatDataIsSerializable(new UnserializableObject(":test3"));
		assertThatDataIsSerializable("~unTILDEtilde~");

	}

	@Test
	public void whenSendingEventWithListOfVariousTypesThenSerializedCorrectly()
			throws Exception {

		assertThatDataIsSerializable(asList("~test", null, 1, true));
		assertThatDataIsNotSerializable(asList(":test", null, 1, true, eventBusManager));
		assertThatDataIsNotSerializable(asList(new Thread()));
		assertThatDataIsNotSerializable(asList("~test", null, 1, true, new UnserializableObject("test")));

		eventBusManager.eventDataSerializer.register(anExtension);

		assertThatDataIsSerializable(asList("~test", null, 1, true, new UnserializableObject("test")));

	}

	@Test
	public void whenSendingEventWithSetOfVariousTypesThenSerializedCorrectly()
			throws Exception {

		assertThatDataIsSerializable(asSet("~test", null, 1, true));
		assertThatDataIsNotSerializable(asSet(":test", null, 1, true, eventBusManager));
		assertThatDataIsNotSerializable(asSet(new Thread()));
		assertThatDataIsNotSerializable(asSet("~test", null, 1, true, new UnserializableObject("test")));

		eventBusManager.eventDataSerializer.register(anExtension);

		assertThatDataIsSerializable(asSet("~test", null, 1, true, new UnserializableObject("test")));

	}

	@Test
	public void whenSendingEventWithMapOfVariousTypesThenSerializedCorrectly()
			throws Exception {

		assertThatDataIsSerializable(asMap("~test", null, 1, true));
		assertThatDataIsNotSerializable(asMap(":test", null, 1, true, eventBusManager, ""));
		assertThatDataIsNotSerializable(asMap(":test", null, 1, true, "", eventBusManager));
		assertThatDataIsNotSerializable(asMap("", new Thread()));
		assertThatDataIsNotSerializable(asMap(new Thread(), ""));
		assertThatDataIsNotSerializable(asMap("~test", null, 1, true, "$", new UnserializableObject("test")));

		eventBusManager.eventDataSerializer.register(anExtension);

		assertThatDataIsSerializable(asSet("~test", null, 1, true, new UnserializableObject("test")));

	}

	@Test
	public void givenComplexStructureOfMapSetAndListThenAllConverted()
			throws Exception {

		Map<Object, Object> map = new HashMap<>();
		map.put("test", asList("1", asList("2")));
		map.put(asSet("t"), null);

		HashMap<Object, Object> map2 = new HashMap<>();
		map2.put("yyyyyy", asList(asList(asList(asList("here", new UnserializableObject("test"))))));
		map.put("nestedMap", map2);

		assertThatDataIsNotSerializable(map);

		eventBusManager.eventDataSerializer.register(anExtension);

		assertThatDataIsSerializable(map);

	}

	EventDataSerializerExtension anExtension = new EventDataSerializerExtension() {
		@Override
		public String getId() {
			return "qwlf";
		}

		@Override
		public Class<?> getSupportedDataClass() {
			return UnserializableObject.class;
		}

		@Override
		public String serialize(Object data) {
			return ((UnserializableObject) data).simpleData;
		}

		@Override
		public Object deserialize(String deserialize) {
			return new UnserializableObject(deserialize);
		}
	};

	// --------------------------------------------------------

	private void assertThatDataIsSerializable(Object data)
			throws IOException, ClassNotFoundException {
		eventBusManager.eventDataSerializer.validateData(data);

		Object serializedData = eventBusManager.eventDataSerializer.serialize(data);

		String base64 = serializeToBase64((Serializable) serializedData);

		Object unserializedData = eventBusManager.eventDataSerializer.deserialize(deserializeBase64(base64));

		assertThat(unserializedData).isEqualTo(data);

	}

	/**
	 * Read the object from Base64 string.
	 */
	private static Object deserializeBase64(String s)
			throws IOException,
				   ClassNotFoundException {
		byte[] data = DatatypeConverter.parseBase64Binary(s);
		ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/**
	 * Write the object to a Base64 string.
	 */
	private static String serializeToBase64(Serializable o)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}

	private void assertThatDataIsNotSerializable(Object o) {
		try {
			eventBusManager.eventDataSerializer.validateData(o);
			fail("Object should be invalid");
		} catch (Exception e) {
			//OK
		}
	}

	public static class UnserializableObject {
		String simpleData;

		public UnserializableObject(String simpleData) {
			this.simpleData = simpleData;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof UnserializableObject)) {
				return false;
			}

			UnserializableObject that = (UnserializableObject) o;

			return simpleData != null ? simpleData.equals(that.simpleData) : that.simpleData == null;
		}

		@Override
		public int hashCode() {
			return simpleData != null ? simpleData.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "UnserializableObject{" +
				   "simpleData='" + simpleData + '\'' +
				   '}';
		}
	}
}
