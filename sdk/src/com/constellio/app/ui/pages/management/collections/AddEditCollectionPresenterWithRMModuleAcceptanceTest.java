package com.constellio.app.ui.pages.management.collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class AddEditCollectionPresenterWithRMModuleAcceptanceTest extends ConstellioTest {
	@Mock
	AddEditCollectionView view;
	@Mock ConstellioNavigator navigator;
	AddEditCollectionPresenter presenter;
	Record zeCollectionRecord;
	private ConstellioModulesManager moduleManager;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		ConstellioFactories constellioFactories = getConstellioFactories();
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);
		doNothing().when(navigator).listFacetConfiguration();

		presenter = new AddEditCollectionPresenter(view, null);
		moduleManager = getAppLayerFactory().getModulesManager();
		zeCollectionRecord = getAppLayerFactory().getCollectionsManager().getCollection(zeCollection).getWrappedRecord();

	}

	@Test
	public void whenActivatingESThenOk()
			throws Exception {
		presenter.updateCollectionModules(zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioESModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id").containsOnly(ConstellioESModule.ID,
				ConstellioRMModule.ID, TaskModule.ID);
	}

	@Test
	public void whenActivatingRobotThenOk()
			throws Exception {
		presenter.updateCollectionModules(zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioRobotsModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id").containsOnly(ConstellioRobotsModule.ID,
				ConstellioRMModule.ID, TaskModule.ID);
	}

}
