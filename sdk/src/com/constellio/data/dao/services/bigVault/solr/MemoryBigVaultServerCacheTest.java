package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class MemoryBigVaultServerCacheTest extends ConstellioTest {

	MemoryBigVaultServerCache cache = new MemoryBigVaultServerCache();
	BigVaultServerCacheValidationResponse callResult;

	@Test
	public void givenRecordsNotInCacheThenRefusedUnlessNew()
			throws Exception {

		callResult = cache.validateVersionsAndLock(asMap("record1", -1L, "record2", -1L));
		assertThat(callResult).is(accepted());

		//Record 3 and 5 are refused, because they are not in cache
		callResult = cache.validateVersionsAndLock(asMap("record3", 3L, "record4", -1L, "record5", 3L));
		assertThat(callResult).has(optimistisLockingFailures("record3", "record5"));

		callResult = cache.validateVersionsAndLock(asMap("record3", -1L, "record4", -1L, "record5", -1L));
		assertThat(callResult).is(accepted());
	}

	@Test
	public void givenNewRecordsIsAcceptedThenCannotBeAcceptedTwice()
			throws Exception {

		callResult = cache.validateVersionsAndLock(asMap("record1", -1L, "record2", -1L, "record3", -1L));
		assertThat(callResult).is(accepted());

		callResult = cache.validateVersionsAndLock(asMap("record1", -1L, "record2", -1L, "record4", -1L));
		assertThat(callResult).is(lockedRecord("record1", "record2"));
	}

	@Test
	public void givenNewRecordsIsAcceptedThenLockReleasedThenCanBeLockedAgainWithValidVersion()
			throws Exception {

		callResult = cache.validateVersionsAndLock(asMap("record1", -1L, "record2", -1L, "record3", -1L));
		assertThat(callResult).is(accepted());

		cache.unlockWithNewVersions(asMap("record1", 12L, "record2", 31L, "record3", 42L));

		callResult = cache.validateVersionsAndLock(asMap("record1", 13L, "record2", 31L, "record3", 45L, "record4", 55L));
		assertThat(callResult).is(optimistisLockingFailures("record1", "record3", "record4"));

		callResult = cache.validateVersionsAndLock(asMap("record1", 12L, "record2", 31L, "record3", 42L));
		assertThat(callResult).is(accepted());
	}

	@Test
	public void givenRecordsAreAlreadyInCacheTheRefusedUnlessSameVersionOrExisting()
			throws Exception {

		cache.insertRecordVersion(asMap("record1", 12L, "record2", 31L, "record3", 42L, "record4", 56L));

		callResult = cache.validateVersionsAndLock(asMap("record1", 13L, "record2", 1L, "record3", -1L, "record4", 56L));
		assertThat(callResult).is(optimistisLockingFailures("record1", "record3"));

		callResult = cache.validateVersionsAndLock(asMap("record1", 12L, "record2", 1L, "record3", 1L, "record4", 56L));
		assertThat(callResult).is(accepted());
	}

	@Test
	public void givenRecordsAreAlreadyInCacheWhenInsertingNewerRecordversionsThenReplaced()
			throws Exception {

		cache.insertRecordVersion(asMap("record1", 12L, "record2", 31L));
		callResult = cache.validateVersionsAndLock(asMap("record1", 13L, "record2", 32L));
		assertThat(callResult).is(optimistisLockingFailures("record1", "record2"));

		cache.insertRecordVersion(asMap("record1", 13L, "record2", 32L));
		callResult = cache.validateVersionsAndLock(asMap("record1", 13L, "record2", 32L));
		assertThat(callResult).is(accepted());
	}

	@Test
	public void givenRecordsAreAlreadyInCacheWhenInsertingOlderRecordversionsThenNotReplaced()
			throws Exception {

		cache.insertRecordVersion(asMap("record1", 12L, "record2", 31L));
		callResult = cache.validateVersionsAndLock(asMap("record1", 11L, "record2", 33L));
		assertThat(callResult).is(optimistisLockingFailures("record1", "record2"));

		cache.insertRecordVersion(asMap("record1", 11L, "record2", 33L));
		callResult = cache.validateVersionsAndLock(asMap("record1", 11L, "record2", 33L));
		assertThat(callResult).is(optimistisLockingFailures("record1"));

		callResult = cache.validateVersionsAndLock(asMap("record1", 12L, "record2", 33L));
		assertThat(callResult).is(accepted());
	}

	private Condition<? super BigVaultServerCacheValidationResponse> accepted() {
		return new Condition<BigVaultServerCacheValidationResponse>() {
			@Override
			public boolean matches(BigVaultServerCacheValidationResponse value) {
				assertThat(value.lockedKeys).describedAs("lockedRecord").isEmpty();
				assertThat(value.keysWithBadVersionAndTheirExpectedVersion).describedAs("keysWithBadVersion").isEmpty();
				assertThat(value.accepted).describedAs("accepted").isTrue();
				return true;
			}
		};

	}

	private Condition<? super BigVaultServerCacheValidationResponse> lockedRecord(final String... records) {
		return new Condition<BigVaultServerCacheValidationResponse>() {
			@Override
			public boolean matches(BigVaultServerCacheValidationResponse value) {
				assertThat(value.accepted).describedAs("accepted").isFalse();
				assertThat(value.lockedKeys).describedAs("lockedRecord").containsOnly(records);
				return true;
			}
		};

	}

	private Condition<? super BigVaultServerCacheValidationResponse> optimistisLockingFailures(final String... records) {
		return new Condition<BigVaultServerCacheValidationResponse>() {
			@Override
			public boolean matches(BigVaultServerCacheValidationResponse value) {
				assertThat(value.accepted).describedAs("accepted").isFalse();
				assertThat(value.keysWithBadVersionAndTheirExpectedVersion.keySet())
						.describedAs("keysWithBadVersion").containsOnly(records);
				return true;
			}
		};

	}
}
