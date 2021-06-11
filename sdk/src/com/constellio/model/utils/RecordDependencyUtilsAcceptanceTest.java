package com.constellio.model.utils;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.Record;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.constellio.model.utils.RecordDependencyUtils.findFirstRecordsForIdSortedIteration;
import static org.apache.ignite.internal.util.lang.GridFunc.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RecordDependencyUtilsAcceptanceTest extends ConstellioTest {

	@Test
	public void whenAllDependentIdsAreLesserThenNoFirstRecords() {

		Stream<Record> records = streamMockedIds(1, 2, 3, 4, 5, 6);

		Function<Record, List<Record>> dependenciesFunction = (record) -> {
			if (record.getRecordId().intValue() == 3) {
				return asList(mock(1));
			}

			if (record.getRecordId().intValue() == 4) {
				return asList(mock(3));
			}
			if (record.getRecordId().intValue() == 5) {
				return asList(mock(3), mock(4));
			}

			return Collections.emptyList();
		};


		assertThat(findFirstRecordsForIdSortedIteration(records, dependenciesFunction)).containsExactly(
				RecordId.toId(1),
				RecordId.toId(3),
				RecordId.toId(4));
	}


	@Test
	public void whenAllDependentIdsAreLesserButNonStandardThenAllFirstRecords() {

		Stream<Record> records = streamMockedIds("1", "2", "3", "4", "5", "6");

		Function<Record, List<Record>> dependenciesFunction = (record) -> {
			if (record.getRecordId().stringValue().equals("3")) {
				return asList(mock("1"));
			}

			if (record.getRecordId().stringValue().equals("4")) {
				return asList(mock("3"));
			}
			if (record.getRecordId().stringValue().equals("5")) {
				return asList(mock("3"), mock("4"));
			}

			return Collections.emptyList();
		};


		assertThat(findFirstRecordsForIdSortedIteration(records, dependenciesFunction)).containsExactly(
				RecordId.toId("1"),
				RecordId.toId("3"),
				RecordId.toId("4")
		);

		//2,5,6 can be indexed later
	}


	@Test
	public void whenAllDependentIdsAreGreaterThenReturnedAsFirstRecords() {

		Stream<Record> records = streamMockedIds(1, 2, 3, 4, 5, 6);

		Function<Record, List<Record>> dependenciesFunction = (record) -> {
			if (record.getRecordId().intValue() == 3) {
				return asList(mock(4));
			}

			if (record.getRecordId().intValue() == 4) {
				return asList(mock(2));
			}
			if (record.getRecordId().intValue() == 5) {
				return asList(mock(1), mock(6));
			}

			return Collections.emptyList();
		};


		assertThat(findFirstRecordsForIdSortedIteration(records, dependenciesFunction)).containsExactly(
				RecordId.toId(2),
				RecordId.toId(4),
				RecordId.toId(1),
				RecordId.toId(6)
		);
	}

	@Test
	public void givenCyclicDependenciesInFirstRecordsThenReturnThemWithoutInfiniteLoop() {

		Stream<Record> records = streamMockedIds(1, 2, 3, 4, 5, 6);

		Function<Record, List<Record>> dependenciesFunction = (record) -> {

			if (record.getRecordId().intValue() == 4) {
				return asList(mock(5));
			}

			if (record.getRecordId().intValue() == 5) {
				return asList(mock(1));
			}

			if (record.getRecordId().intValue() == 1) {
				return asList(mock(4));
			}

			return Collections.emptyList();
		};


		assertThat(findFirstRecordsForIdSortedIteration(records, dependenciesFunction))
				.containsOnly(
						RecordId.toId(4),
						RecordId.toId(1),
						RecordId.toId(5)
				);
	}


	Record mock(int id) {
		Record record = Mockito.mock(Record.class);
		RecordId recordId = RecordId.id(id);
		when(record.getRecordId()).thenReturn(recordId);
		return record;
	}

	Record mock(String id) {
		Record record = Mockito.mock(Record.class);
		RecordId recordId = RecordId.id(id);
		when(record.getRecordId()).thenReturn(recordId);
		return record;
	}

	Stream<Record> streamMockedIds(int... ids) {
		List<Record> records = new ArrayList<>();
		Arrays.stream(ids).forEach(id -> {
			records.add(mock(id));
		});
		return records.stream();
	}


	Stream<Record> streamMockedIds(String... ids) {
		List<Record> records = new ArrayList<>();
		Arrays.stream(ids).forEach(id -> {
			records.add(mock(id));
		});
		return records.stream();
	}

}
