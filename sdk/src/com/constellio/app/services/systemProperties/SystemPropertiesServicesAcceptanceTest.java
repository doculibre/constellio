package com.constellio.app.services.systemProperties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;

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
