/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.sdk;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;

public class TestConnectorEvent {

	public static final String ADD_EVENT = "add";
	public static final String MODIFY_EVENT = "addmodify";
	public static final String DELETE_EVENT = "delete";

	String eventType;

	Record record;

	List<Metadata> toStringMetadatas = new ArrayList<>();

	private TestConnectorEvent(String eventType, Record record) {
		this.eventType = eventType;
		this.record = record;
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

	public static TestConnectorEvent addEvent(Record record) {
		return new TestConnectorEvent(ADD_EVENT, record);
	}

	public static TestConnectorEvent addEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(ADD_EVENT, recordWrapper.getWrappedRecord());
	}

	public static TestConnectorEvent modifyEvent(Record record) {
		return new TestConnectorEvent(MODIFY_EVENT, record);
	}

	public static TestConnectorEvent modifyEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(MODIFY_EVENT, recordWrapper.getWrappedRecord());
	}

	public static TestConnectorEvent deleteEvent(Record record) {
		return new TestConnectorEvent(DELETE_EVENT, record);
	}

	public static TestConnectorEvent deleteEvent(RecordWrapper recordWrapper) {
		return new TestConnectorEvent(DELETE_EVENT, recordWrapper.getWrappedRecord());
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
