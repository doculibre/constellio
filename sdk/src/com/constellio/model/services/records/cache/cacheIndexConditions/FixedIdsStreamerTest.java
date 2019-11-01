package com.constellio.model.services.records.cache.cacheIndexConditions;

import com.constellio.model.services.records.IntegerRecordId;
import com.constellio.model.services.records.StringRecordId;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.constellio.data.utils.LangUtils.stream;
import static com.constellio.model.services.records.RecordUtils.toStringId;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class FixedIdsStreamerTest {

	@Test
	public void whenCreatingStreamerFromStringValuesThenStreamedIdsAreSorted() {

		FixedIdsStreamer streamer = FixedIdsStreamer.createFromStringValues(
				asList(toStringId(3), toStringId(1), toStringId(2), "1", "2", "42", toStringId(666)));

		assertThat(stream(streamer.iterator()).collect(Collectors.toList())).containsExactly(
				new IntegerRecordId(1),
				new IntegerRecordId(2),
				new IntegerRecordId(3),
				new IntegerRecordId(666),
				new StringRecordId("1"),
				new StringRecordId("2"),
				new StringRecordId("42")
		);
	}

	@Test
	public void whenCreatingStreamerFromRecordIdsThenStreamedIdsAreSorted() {

		FixedIdsStreamer streamer = FixedIdsStreamer.createFromRecordIds(
				asList(new IntegerRecordId(3), new IntegerRecordId(1), new IntegerRecordId(2), new StringRecordId("1"),
						new StringRecordId("2"), new StringRecordId("42"), new IntegerRecordId(666)));

		assertThat(stream(streamer.iterator()).collect(Collectors.toList())).containsExactly(
				new IntegerRecordId(1),
				new IntegerRecordId(2),
				new IntegerRecordId(3),
				new IntegerRecordId(666),
				new StringRecordId("1"),
				new StringRecordId("2"),
				new StringRecordId("42")
		);
	}
}
