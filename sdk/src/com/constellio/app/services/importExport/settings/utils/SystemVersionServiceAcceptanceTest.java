package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsExportOptions;
import com.constellio.app.services.importExport.settings.model.ImportedSystemVersion;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemVersionServiceAcceptanceTest extends ConstellioTest {

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule(),
				withCollection("anotherCollection").withConstellioRMModule().withConstellioESModule());
	}

	@Test
	public void whenCreatingAVersionExportThenVersionSystemIsValid() {
		SettingsExportOptions settingsExportOptions = new SettingsExportOptions();
		settingsExportOptions.setOnlyUSR(true);
		SystemVersionService systemVersionService = new SystemVersionService(getAppLayerFactory());
		ImportedSystemVersion importedSystemVersion = systemVersionService.getSystemVersion(settingsExportOptions);

		assertThat(importedSystemVersion).isNotNull();
		assertThat(importedSystemVersion.getFullVersion()).isNotNull();
		assertThat(importedSystemVersion.getPlugins()).isEmpty();
	}

}
