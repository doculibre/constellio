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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.ListAssert;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class ESTestUtils {

	public static ObservedEventsAssert assertThatEventsObservedBy(TestConnectorEventObserver observer) {
		return new ObservedEventsAssert(observer.getEvents());
	}

	public static class ObservedEventsAssert extends ListAssert<TestConnectorEvent> {

		List<Metadata> toStringMetadatas = new ArrayList<>();
		List<TestConnectorEvent> events;

		public ObservedEventsAssert(List<TestConnectorEvent> events) {
			super(events);
			this.events = events;
		}

		public ObservedEventsAssert comparingRecordsUsing(List<Metadata> metadatas) {
			super.usingElementComparator(new ObservedEventsComparator(metadatas));
			return this;
		}

		public ObservedEventsAssert comparingRecordsUsing(Metadata... metadatas) {
			this.toStringMetadatas = asList(metadatas);
			for (TestConnectorEvent event : events) {
				event.setToStringMetadatas(toStringMetadatas);
			}
			super.usingElementComparator(new ObservedEventsComparator(asList(metadatas)));
			return this;
		}

		private void applyTOStringMetadatasToArray(TestConnectorEvent... values) {
			for (TestConnectorEvent value : values) {
				value.setToStringMetadatas(toStringMetadatas);
			}
		}

		private void applyTOStringMetadatasToIterable(Iterable<? extends TestConnectorEvent> values) {
			for (TestConnectorEvent value : values) {
				value.setToStringMetadatas(toStringMetadatas);
			}
		}

		@Override
		public ListAssert<TestConnectorEvent> contains(TestConnectorEvent... values) {
			applyTOStringMetadatasToArray(values);
			return super.contains(values);
		}

		@Override
		public ListAssert<TestConnectorEvent> containsOnly(TestConnectorEvent... values) {
			applyTOStringMetadatasToArray(values);
			return super.containsOnly(values);
		}

		@Override
		public ListAssert<TestConnectorEvent> containsOnlyOnce(TestConnectorEvent... values) {
			applyTOStringMetadatasToArray(values);
			return super.containsOnlyOnce(values);
		}

		@Override
		public ListAssert<TestConnectorEvent> containsExactly(TestConnectorEvent... values) {
			applyTOStringMetadatasToArray(values);
			return super.containsExactly(values);
		}

		@Override
		public ListAssert<TestConnectorEvent> containsSequence(TestConnectorEvent... sequence) {
			applyTOStringMetadatasToArray(sequence);
			return super.containsSequence(sequence);
		}

		@Override
		public ListAssert<TestConnectorEvent> containsSubsequence(TestConnectorEvent... sequence) {
			applyTOStringMetadatasToArray(sequence);
			return super.containsSubsequence(sequence);
		}

		@Override
		public ListAssert<TestConnectorEvent> doesNotContain(TestConnectorEvent... values) {
			applyTOStringMetadatasToArray(values);
			return super.doesNotContain(values);
		}

		@Override
		public ListAssert<TestConnectorEvent> startsWith(TestConnectorEvent... sequence) {
			applyTOStringMetadatasToArray(sequence);
			return super.startsWith(sequence);
		}

		@Override
		public ListAssert<TestConnectorEvent> endsWith(TestConnectorEvent... sequence) {
			applyTOStringMetadatasToArray(sequence);
			return super.endsWith(sequence);
		}

	}

	private static class ObservedEventsComparator implements Comparator<TestConnectorEvent> {

		List<Metadata> metadatas;

		private ObservedEventsComparator(List<Metadata> metadatas) {
			this.metadatas = metadatas;
		}

		@Override
		public int compare(TestConnectorEvent event1, TestConnectorEvent event2) {

			if (!event1.eventType.equals(event2.eventType)) {
				return 1;
			}

			List<Object> event1Values = getMetadatasValues(event1.record);
			List<Object> event2Values = getMetadatasValues(event2.record);
			return event1Values.equals(event2Values) ? 0 : 1;
		}

		private List<Object> getMetadatasValues(Record record) {

			List<Object> values = new ArrayList<>();

			for (Metadata metadata : metadatas) {
				if (metadata.isMultivalue()) {
					values.add(record.getList(metadata));
				} else {
					values.add(record.get(metadata));
				}
			}

			return values;
		}
	}
}
