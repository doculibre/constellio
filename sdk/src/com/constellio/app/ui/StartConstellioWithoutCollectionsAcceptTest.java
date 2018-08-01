package com.constellio.app.ui;

import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.sdk.tests.AppLayerConfigurationAlteration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import org.junit.Test;

@UiTest
@MainTest
public class StartConstellioWithoutCollectionsAcceptTest extends ConstellioTest {

	@Test
	@MainTestDefaultStart
	public void startOnHomePage()
			throws Exception {
		givenTransactionLogIsEnabled();

		ModelLayerConfiguration config = getModelLayerFactory().getConfiguration();
		//		doCallRealMethod().when(config).getMainDataLanguage();

		newWebDriver();

		waitUntilICloseTheBrowsers();
	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageWithAllLanguagesEnabled()
			throws Exception {
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setEnabledPrototypeLanguages("ar");
			}
		});
		givenTransactionLogIsEnabled();

		ModelLayerConfiguration config = getModelLayerFactory().getConfiguration();
		//		doCallRealMethod().when(config).getMainDataLanguage();

		newWebDriver();

		waitUntilICloseTheBrowsers();
	}

}
