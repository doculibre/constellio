package com.constellio.model.services.configs;

import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class ConstellioEIMSystemConfigurationsAcceptanceTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";
	String aThirdCollection = "aThirdCollection";

	static SystemConfigurationsManager manager;

	@Before
	public void setUp()
			throws Exception {

		givenSpecialCollection(zeCollection).withAllTestUsers();
		givenSpecialCollection(anotherCollection).withAllTestUsers();
		givenSpecialCollection(aThirdCollection).withAllTestUsers();

		manager = getModelLayerFactory().getSystemConfigurationsManager();
	}

	@Test
	public void whenWriteZZRecordsThenSetConfigurationInDataLayerConfiguration()
			throws Exception {

		manager.setValue(ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG, true);
		assertThat(getDataLayerFactory().getDataLayerConfiguration().isWriteZZRecords()).isTrue();

		manager.setValue(ConstellioEIMConfigs.WRITE_ZZRECORDS_IN_TLOG, false);
		assertThat(getDataLayerFactory().getDataLayerConfiguration().isWriteZZRecords()).isFalse();
	}
}
