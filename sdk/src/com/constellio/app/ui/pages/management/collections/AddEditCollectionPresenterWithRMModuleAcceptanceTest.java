package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.CollectionVODataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class AddEditCollectionPresenterWithRMModuleAcceptanceTest extends ConstellioTest {
	@Mock
	AddEditCollectionView view;
	MockedNavigation navigator;
	AddEditCollectionPresenter presenter;
	Record zeCollectionRecord;
	private ConstellioModulesManager moduleManager;
	@Mock
	CollectionVODataProvider.CollectionVO collectionVO;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		navigator = new MockedNavigation();

		ConstellioFactories constellioFactories = getConstellioFactories();
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		presenter = new AddEditCollectionPresenter(view, null);
		moduleManager = getAppLayerFactory().getModulesManager();
		zeCollectionRecord = getAppLayerFactory().getCollectionsManager().getCollection(zeCollection).getWrappedRecord();
		doReturn(null).when(collectionVO).getConservationCalendarNumber();
		doReturn(null).when(collectionVO).getOrganizationNumber();

	}

	@Test
	public void whenActivatingESThenOk()
			throws Exception {
		presenter.updateCollectionModules(collectionVO, zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioESModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id").containsOnly(ConstellioESModule.ID,
				ConstellioRMModule.ID, TaskModule.ID);
	}

	@Test
	public void whenActivatingRobotThenOk()
			throws Exception {
		presenter.updateCollectionModules(collectionVO, zeCollectionRecord, zeCollection, new HashSet<>(asList(ConstellioRobotsModule.ID)));
		assertThat(moduleManager.getEnabledModules(zeCollection)).extracting("id").containsOnly(ConstellioRobotsModule.ID,
				ConstellioRMModule.ID, TaskModule.ID);
	}

}
