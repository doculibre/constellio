package com.constellio.model.services.records.cache.offHeapCollections;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import static com.constellio.data.dao.dto.records.RecordDTOUtils.toStringId;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SortedStringAndIntIdsListTest extends ConstellioTest {

	@Test
	public void test() {

		SortedStringIdsList list = new SortedStringIdsList();

		list.add(toStringId(12));
		list.add(toStringId(13));
		list.add(toStringId(42_000_000));
		list.add(toStringId(12));
		list.add("13");
		list.add("2");
		list.add("42");
		list.add("11");
		list.add("12");
		list.add("2");

		assertThat(list.size()).isEqualTo(8);
		assertThat(list.getValues()).isEqualTo(asList(toStringId(12), toStringId(13), toStringId(42_000_000), "11", "12", "13", "2", "42"));

		list.remove("13");
		list.remove(toStringId(12));

		assertThat(list.size()).isEqualTo(6);
		assertThat(list.getValues()).isEqualTo(asList(toStringId(13), toStringId(42_000_000), "11", "12", "2", "42"));

		list.clear();

		assertThat(list.size()).isEqualTo(0);
		assertThat(list.getValues()).isEmpty();
	}
}
