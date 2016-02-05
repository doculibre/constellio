package com.constellio.app.ui.pages.management.collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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

public class AddEditCollectionPresenterWithRobotModuleAcceptanceTest extends ConstellioTest {
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
				withZeCollection().withRobotsModule().withAllTestUsers()
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
	public void whenActivatingRMThenOk()
			throws Exception {
		presenter.updateCollectionModules(zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioRMModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id")
				.containsOnly(ConstellioRMModule.ID, TaskModule.ID, ConstellioRobotsModule.ID);
	}

	@Test
	public void whenActivatingTaskThenOk()
			throws Exception {
		presenter.updateCollectionModules(zeCollectionRecord, zeCollection, new HashSet<>(asList(TaskModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id")
				.containsOnly(ConstellioRobotsModule.ID, TaskModule.ID);
	}

	@Test
	public void whenActivatingESThenOk()
			throws Exception {
		presenter.updateCollectionModules(zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioESModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id")
				.containsOnly(ConstellioRobotsModule.ID, ConstellioESModule.ID);
	}

}