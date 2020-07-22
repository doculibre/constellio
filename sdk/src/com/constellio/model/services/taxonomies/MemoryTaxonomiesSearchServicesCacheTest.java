package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.records.Record;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoryTaxonomiesSearchServicesCacheTest extends ConstellioTest {

	MemoryTaxonomiesSearchServicesCache cache = new MemoryTaxonomiesSearchServicesCache();

	@Before
	public void setUp()
			throws Exception {
		cache.insert("chuck", "001", "mode1", true);
		cache.insert("dakota", "002", "mode1", false);
		cache.insert("bob", "003", "mode1", true);
		cache.insert("chuck", "001", "mode2", false);
		cache.insert("dakota", "002", "mode2", true);
		cache.insert("bob", "003", "mode2", false);
		cache.insert("chuck", "001", "mode3", true);
		cache.insert("dakota", "002", "mode3", false);
		cache.insert("bob", "003", "mode3", true);
	}

	@Test
	public void whenInsertingValuesThenRetrievable()
			throws Exception {

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);

		assertThat(cache.getCachedValue("bob", "003", "mode4")).isEqualTo(null);
		assertThat(cache.getCachedValue("alice", "003", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "004", "mode3")).isEqualTo(null);

	}

	@Test
	public void whenInvalidatingAllThenAllInvalidated()
			throws Exception {

		cache.invalidateAll();

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(null);
	}

	@Test
	public void whenInvalidatingNodeWithChildrenThenOnlyThisNodeIsInvalidated()
			throws Exception {

		cache.invalidateWithChildren("001");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);

		cache.invalidateWithChildren("002");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);
	}

	@Test
	public void whenInvalidatingNodeWithoutChildrenThenOnlyThisNodeIsInvalidated()
			throws Exception {

		cache.invalidateWithoutChildren("001");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);

		cache.invalidateWithoutChildren("002");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(true);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);
	}

	@Test
	public void whenInvalidatingCacheForUserThenOnlyNodeOfThisUserIsInvalidated()
			throws Exception {

		cache.invalidateUser("chuck");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);

		cache.invalidateUser("dakota");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);
	}

	@Test
	public void whenInvalidatingCacheForRecordIdThenOnlyNodeOfThisRecordIsInvalidated()
			throws Exception {

		cache.invalidateRecord("001");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(true);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(false);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);

		cache.invalidateRecord("002");

		assertThat(cache.getCachedValue("chuck", "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode1")).isEqualTo(true);
		assertThat(cache.getCachedValue("chuck", "001", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode2")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode2")).isEqualTo(false);
		assertThat(cache.getCachedValue("chuck", "001", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("dakota", "002", "mode3")).isEqualTo(null);
		assertThat(cache.getCachedValue("bob", "003", "mode3")).isEqualTo(true);
	}

	@Test
	public void whenCallingServiceWithNullValuesThenDoNothing()
			throws Exception {

		cache.insert(null, "001", "mode1", true);
		cache.insert("chuck", null, "mode1", true);
		cache.insert("chuck", "001", null, true);
		cache.insert("chuck", "001", "mode1", null);

		assertThat(cache.getCachedValue(null, "001", "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("chuck", (String) null, "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("chuck", (Record) null, "mode1")).isEqualTo(null);
		assertThat(cache.getCachedValue("chuck", "001", null)).isEqualTo(null);

		cache.invalidateRecord(null);
		cache.invalidateUser(null);
		cache.invalidateWithoutChildren(null);
		cache.invalidateWithChildren(null);

	}
}
