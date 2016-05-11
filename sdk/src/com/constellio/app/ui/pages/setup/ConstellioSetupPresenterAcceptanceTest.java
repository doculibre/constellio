package com.constellio.app.ui.pages.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@InDevelopmentTest
public class ConstellioSetupPresenterAcceptanceTest extends ConstellioTest {

	@Mock ConstellioSetupView view;

	@Test
	public void testName()
			throws Exception {
		when(view.getSessionContext()).thenReturn(FakeSessionContext.noUserNoCollection());
		ConstellioFactories constellioFactories = getConstellioFactories();
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		ConstellioSetupPresenter presenter = new ConstellioSetupPresenter(view);
		presenter.saveRequested("fr", Arrays.asList("fr", "en"), Arrays.asList("rm"), null, "zeColl", "supertimor");
		CollectionsManager collectionsManager = getAppLayerFactory().getCollectionsManager();
		Collection zeCollectionRecord = collectionsManager.getCollection("zeColl");
		assertThat(zeCollectionRecord.getCode()).isEqualTo("zeColl");
		assertThat(zeCollectionRecord.getName()).isEqualTo("zeColl");
		assertThat(zeCollectionRecord.getLanguages()).containsOnly("fr", "en");
		assertThat(getAppLayerFactory().getModulesManager().getEnabledModules("zeColl")).extracting("id").containsOnly("rm", TaskModule.ID);

		newWebDriver();
		waitUntilICloseTheBrowsers();
	}
}
