package com.constellio.app.modules.es.sdk;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.CollectionObject;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;

public class TestConnectorEvent {

	public static final String ADD_EVENT = "add";
	public static final String MODIFY_EVENT = "addmodify";
	public static final String DELETE_EVENT = "delete";

	ESSchemasRecordsServices es;

	String eventType;

	Record record;

	String url;

	LocalDateTime fetchedDateTime;

	List<Metadata> toStringMetadatas = new ArrayList<>();

	private TestConnectorEvent(String eventType, Record record, ESSchemasRecordsServices es) {
		this.es = es;
		this.eventType = eventType;
		this.record = record;

		this.url = record.get(es.connectorDocument.url());
		this.fetchedDateTime = record.get(es.connectorDocument.fetchedDateTime());
	}

	public void setToStringMetadatas(List<Metadata> toStringMetadatas) {
		this.toStringMetadatas = toStringMetadatas;
	}

	public String getEventType() {
		return eventType;
	}

	public Record getRecord() {
		return record;
	}

	public String getUrl() {
		return url;
	}

	public LocalDateTime getFetchedDateTime() {
		return fetchedDateTime;
	}

	private static ESSchemasRecordsServices es(CollectionObject object) {
		return new ESSchemasRecordsServices(object.getCollection(), ConstellioFactories.getInstance().getAppLayerFactory());
	}

	public static TestConnectorEvent addEvent(Record record) {

		return new TestConnectorEvent(ADD_EVENT, record, es(record));
	}

	public static TestConnectorEvent addEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(ADD_EVENT, recordWrapper.getWrappedRecord(), es(recordWrapper));
	}

	public static TestConnectorEvent modifyEvent(Record record) {
		return new TestConnectorEvent(MODIFY_EVENT, record, es(record));
	}

	public static TestConnectorEvent modifyEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(MODIFY_EVENT, recordWrapper.getWrappedRecord(), es(recordWrapper));
	}

	public static TestConnectorEvent deleteEvent(Record record) {
		return new TestConnectorEvent(DELETE_EVENT, record, es(record));
	}

	public static TestConnectorEvent deleteEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(DELETE_EVENT, recordWrapper.getWrappedRecord(), es(recordWrapper));
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder(eventType + " record[");
		boolean first = true;
		for (Metadata metadata : toStringMetadatas) {

			Object value;
			if (metadata.isMultivalue()) {
				value = record.getList(metadata);
			} else {
				value = record.get(metadata);
			}
			if (!first) {
				sb.append(", ");
			}

			sb.append(metadata.getLocalCode() + "=" + value);
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}
}
