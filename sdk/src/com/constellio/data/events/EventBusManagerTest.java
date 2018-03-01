package com.constellio.data.events;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_EventBusAlreadyExist;
import com.constellio.data.events.EventBusManagerRuntimeException.EventBusManagerRuntimeException_NoSuchEventBus;
import com.constellio.sdk.tests.ConstellioTest;

public class EventBusManagerTest extends ConstellioTest {

	@Mock EventBusSendingService sendingService;
	EventBusManager eventBusManager;

	@Mock EventBusListener listener1, listener2, listener3, listener4;

	@Before
	public void setUp()
			throws Exception {
		eventBusManager = new EventBusManager(sendingService);
	}

	@Test
	public void validatingAddRemoveEventBusOperations()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique");
		EventBus schoolBus = eventBusManager.createEventBus("Bus d'école");

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
			eventBusManager.createEventBus("Bus magique");
			fail("Exception expected");
		} catch (EventBusManagerRuntimeException_EventBusAlreadyExist e) {

		}

		eventBusManager.removeEventBus("Bus magique");
		assertThat(eventBusManager.hasEventBus("Bus magique")).isFalse();

		EventBus newMagicBus = eventBusManager.createEventBus("Bus magique");
		assertThat(newMagicBus).isNotSameAs(magicBus);

	}

	@Test
	public void givenMultipleEventBusReceivedEventsOrDispatchedToTheGoodOneAndEventWithUnknownBusAreDismissed()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique");
		magicBus.register(listener1);
		magicBus.register(listener2);
		magicBus.register(listener4);
		magicBus.unregister(listener4);

		EventBus schoolBus = eventBusManager.createEventBus("Bus d'école");

		EventBus ourleheinExpressBus = eventBusManager.createEventBus("Ourléhein Express");
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
	public void whenSendingEventThenValidatedThenSentLocallyThenSentRemotely()
			throws Exception {

		EventBus magicBus = eventBusManager.createEventBus("Bus magique");
		magicBus.register(listener1);
		magicBus.register(listener2);

		eventBusManager = spy(eventBusManager);
		eventBusManager.eventDataSerializer = spy(eventBusManager.eventDataSerializer);

		Event event1 = new Event("Bus magique", "flying", "1", 1l, "zeValue");
		eventBusManager.send(event1);

		Event event2 = new Event("Bus magique", "landing", "1", 1l, "anotherValue");
		eventBusManager.send(event2);

		InOrder inOrder = Mockito
				.inOrder(listener1, listener2, eventBusManager.eventDataSerializer, eventBusManager, sendingService);
		inOrder.verify(eventBusManager.eventDataSerializer).validateData("zeValue");
		inOrder.verify(eventBusManager).receive(event1);
		inOrder.verify(sendingService).sendRemotely(event1);
		inOrder.verify(eventBusManager.eventDataSerializer).validateData("anotherValue");
		inOrder.verify(eventBusManager).receive(event2);
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

		eventBusManager.eventDataSerializer.register(new EventDataSerializerExtension() {
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
		});
		assertThatDataIsSerializable(new UnserializableObject("test"));
		assertThatDataIsSerializable(new UnserializableObject("~test2"));
		assertThatDataIsSerializable(new UnserializableObject(":test3"));
		assertThatDataIsSerializable("~unTILDEtilde~");

	}

	@Test
	public void whenSendingEventWithListOfVariousTypesThenSerializedCorrectly()
			throws Exception {

	}

	// --------------------------------------------------------

	private void assertThatDataIsSerializable(Object data) {
		eventBusManager.eventDataSerializer.validateData(data);

		Object serializedData = eventBusManager.eventDataSerializer.serialize(data);
		Object unserializedData = eventBusManager.eventDataSerializer.deserialize(serializedData);

		assertThat(unserializedData).isEqualTo(data);

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
			if (this == o)
				return true;
			if (!(o instanceof UnserializableObject))
				return false;

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
