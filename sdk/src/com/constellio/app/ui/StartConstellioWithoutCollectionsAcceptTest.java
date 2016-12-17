package com.constellio.app.ui;

import static org.mockito.Mockito.doCallRealMethod;

import org.junit.Test;

import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;

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

}
