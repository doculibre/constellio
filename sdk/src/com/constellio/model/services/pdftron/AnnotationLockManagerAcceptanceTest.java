package com.constellio.model.services.pdftron;

import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCache;
import com.constellio.data.events.EventBus;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class AnnotationLockManagerAcceptanceTest extends ConstellioTest {

	public static final String TEST_HASH_1 = "dfsgdfklshgklfds324";
	public static final String TEST_HASH_2 = "hgfjhghjfdfgdsfdsdfs";


	IOServices ioServices;

	HashingService hashService;

	File root;

	@Mock EventBus eventBus;
	ConstellioCache cache;
	FileSystemConfigManager configManager;
	AnnotationLockManager annotationLockManager;


	@Before
	public void setUp() {

		hashService = getIOLayerFactory().newHashingService(BASE64_URL_ENCODED);

		root = newTempFolder();

		ioServices = getIOLayerFactory().newIOServices();

		cache = new SerializationCheckCache("zeCache", new ConstellioCacheOptions());
		configManager = spy(
				new FileSystemConfigManager(root, ioServices, hashService, cache, getDataLayerFactory().getExtensions(), eventBus));

		annotationLockManager = new AnnotationLockManager(configManager);
	}

	@Test
	public void whenTrySameLockThenOnlyFirstOneWork() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock3")).isFalse();
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "46", "lock2")).isFalse();
	}

	@Test
	public void whenGetLockUserTwiceThenFirstPageIdIsFirstOne() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock2")).isFalse();

		assertThat(annotationLockManager.getPageIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("lock");

	}

	@Test
	public void getLockUserThenWhenAsLockThenReturnUserId() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();

		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("43");
	}


	@Test
	public void whenLockedAndReleaseWithWrongPageIdThenNothingHappen() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();

		annotationLockManager.releaseLock(TEST_HASH_1, "01", "1.0", "43", "lock2");

		assertThat(annotationLockManager.getPageIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("lock");
		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("43");

		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isFalse();
	}

	@Test
	public void whenLockingAndUnlockingSameParamsThenLockingWorkAgain() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock2")).isFalse();

		annotationLockManager.releaseLock(TEST_HASH_1, "01", "1.0", "43", "lock");

		assertThat(annotationLockManager.getPageIdOfLock(TEST_HASH_1, "01", "1.0")).isNull();
		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_1, "01", "1.0")).isNull();


		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock4")).isTrue();
	}

	@Test
	public void whenMultipleLockThenTheyDontAffectEachOther() {
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock")).isTrue();
		assertThat(annotationLockManager.obtainLock(TEST_HASH_2, "01", "1.0", "43", "lock")).isTrue();

		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_2, "01", "1.0")).isEqualTo("43");
		assertThat(annotationLockManager.getPageIdOfLock(TEST_HASH_2, "01", "1.0")).isEqualTo("lock");

		annotationLockManager.releaseLock(TEST_HASH_2, "01", "1.0", "43", "lock");
		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_2, "01", "1.0")).isNull();

		assertThat(annotationLockManager.getUserIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("43");
		assertThat(annotationLockManager.getPageIdOfLock(TEST_HASH_1, "01", "1.0")).isEqualTo("lock");

		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "02", "1.0", "43", "lock")).isTrue();

		// Only one user can obtain the lock (hash + recordId + version)
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "444", "lock")).isFalse();

		// different page id. you cannot obtain it if an other page already have it.
		assertThat(annotationLockManager.obtainLock(TEST_HASH_1, "01", "1.0", "43", "lock2")).isFalse();
	}
}
