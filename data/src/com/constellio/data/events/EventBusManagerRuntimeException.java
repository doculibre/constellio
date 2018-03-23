package com.constellio.data.events;

public class EventBusManagerRuntimeException extends RuntimeException {

	public EventBusManagerRuntimeException(String message) {
		super(message);
	}

	public EventBusManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class EventBusManagerRuntimeException_NoSuchEventBus extends EventBusManagerRuntimeException {

		public EventBusManagerRuntimeException_NoSuchEventBus(String busName) {
			super("No event bus with name '" + busName + "'");
		}
	}

	public static class EventBusManagerRuntimeException_EventBusAlreadyExist extends EventBusManagerRuntimeException {

		public EventBusManagerRuntimeException_EventBusAlreadyExist(String busName) {
			super("Event bus with name '" + busName + "' already exist");
		}
	}

	public static class EventBusManagerRuntimeException_DataIsNotSerializable extends EventBusManagerRuntimeException {

		public EventBusManagerRuntimeException_DataIsNotSerializable(Class<?> classData) {
			super("Data of class '" + classData + "' cannot be used in a event, "
					+ "since it is not serializable and no EventDataSerializer have been configured supporting this class");
		}

		public EventBusManagerRuntimeException_DataIsNotSerializable(Event event, Throwable t) {
			super("Event '" + event.busName + ":" + event.type + "' with data '" + event.getData()
					+ "' cannot be used in a event,  since it is not serializable", t);
		}
	}
}
