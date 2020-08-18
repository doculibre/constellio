package com.constellio.app.services.systemProperties;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemPropertiesServicesAcceptanceTest extends ConstellioTest {
	SystemPropertiesServices systemPropertiesServices;

	@Before
	public void setUp()
			throws Exception {
		systemPropertiesServices = new SystemPropertiesServices(new FoldersLocator(), getIOLayerFactory().newIOServices());
	}

	@Test
	public void whenIsValidWarThenOk()
			throws Exception {
		assertThat(systemPropertiesServices.isFreeSpaceInTempFolderLowerThan(1)).isFalse();
		assertThat(systemPropertiesServices.isAvailableMemoryLowerThan(1)).isFalse();
	}

}
