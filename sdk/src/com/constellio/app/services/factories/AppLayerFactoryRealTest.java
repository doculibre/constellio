package com.constellio.app.services.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;

public class AppLayerFactoryRealTest extends ConstellioTest {

	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	@Mock AppLayerConfiguration appLayerConfiguration;

	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;

	AppLayerFactory factory;

	@Before
	public void setUp()
			throws Exception {

		modelLayerFactory = getModelLayerFactory();
		dataLayerFactory = getDataLayerFactory();

		factory = spy(new AppLayerFactory(appLayerConfiguration, modelLayerFactory, dataLayerFactory, statefullServiceDecorator));
	}

	@Test
	public void whenGetPluginManagerThenAlwaysSameInstance()
			throws Exception {

		ConstellioPluginManager manager1 = factory.getPluginManager();
		ConstellioPluginManager manager2 = factory.getPluginManager();

		assertThat(manager1).isNotNull().isSameAs(manager2);

	}

	@Test
	public void whenCollectionsServicesThenNotNull()
			throws Exception {

		assertThat(factory.getCollectionsManager()).isNotNull();
	}

	@Test
	public void whenNewMigrationServicesThenNotNull() {
		assertThat(factory.newMigrationServices()).isNotNull();
	}

	@Test
	public void whenGetModulesManagerThenSameInstance()
			throws Exception {

		ConstellioModulesManagerImpl modulesManager1 = (ConstellioModulesManagerImpl) factory.getModulesManager();
		ConstellioModulesManagerImpl modulesManager2 = (ConstellioModulesManagerImpl) factory.getModulesManager();

		assertThat(modulesManager1).isNotNull().isSameAs(modulesManager2);

	}

	@Test
	public void whenGetLabelTemplateManagerThenAlwaysSameInstance()
			throws Exception {

		LabelTemplateManager manager1 = factory.getLabelTemplateManager();
		LabelTemplateManager manager2 = factory.getLabelTemplateManager();

		assertThat(manager1).isNotNull().isSameAs(manager2);

	}

	//TODO
	@Test
	public void givenPluginWithExceptionDuringMigrateWhenInitializeThenConstellioRestarted()
			throws AppManagementServiceException {
		//fail();
		//factory.initialize();
		//verify(factory, times(1)).restart();
	}

	//TODO
	@Test
	public void givenPluginWithExceptionDuringStartWhenInitializeThenConstellioRestarted()
			throws AppManagementServiceException {
		//fail();
		//factory.initialize();
		//verify(factory, times(1)).restart();
	}

	@Test
	public void givenValidPluginWhenInitializeThenConstellioNotRestarted()
			throws AppManagementServiceException {
		//fail();
		//factory.initialize();
		//verify(factory, times(0)).restart();
	}
}
